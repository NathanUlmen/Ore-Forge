package ore.forge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.mongodb.client.*;
import ore.forge.Items.*;
import org.bson.Document;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**@author Nathan Ulmen
 * The Item Manager is responsible for verifying and loading Item data and keeping track of all items.
 * */
public class ItemManager {
    private final HashMap<String, Item> allItems;
    private int loadCount;

    public ItemManager(GameWorld gameWorld) {
        Gdx.app.log("Resource Manager", "Initializing Resource Manager...");
        loadCount = 0;
        allItems = new HashMap<>();

        Stopwatch stopwatch = new Stopwatch(TimeUnit.MILLISECONDS);
        stopwatch.start();
//        mongoConnect();

        stopwatch.restart();
        loadItems(Constants.CONVEYORS_FP, gameWorld);
        loadItems(Constants.DROPPERS_FP, gameWorld);
        loadItems(Constants.UPGRADER_FP, gameWorld);
        loadItems(Constants.FURNACE_FP, gameWorld);
        stopwatch.stop();
        Gdx.app.log("Resource Manager", Color.highlightString("Loaded: " + loadCount + " items in " + stopwatch.getElapsedTime() + " ms.", Color.GREEN));
    }

    public HashMap<String, Item> getAllItems() {
        return allItems;
    }

    public HashMap<String, Item> copyAllItems() {
        return new HashMap<>(allItems);
    }

    public Item getItem(String id) {
        if (allItems.containsKey(id)) {
            return allItems.get(id);
        }
        throw new IllegalArgumentException("Item with ID: " + id + " not found.");
    }

    private void loadItems(String fileToParse, GameWorld gameWorld) {
        JsonReader jsonReader = new JsonReader();
        JsonValue fileContents = jsonReader.parse(Gdx.files.local(fileToParse));
        fileContents.child().remove();//Remove version field from file.
        switch (fileToParse) {
            case Constants.DROPPERS_FP:
                for (JsonValue jsonValue : fileContents) {
                    addToAllItems(new Dropper(jsonValue, gameWorld), Color.PINK);
                }
                break;
            case Constants.FURNACE_FP:
                for (JsonValue jsonValue : fileContents) {
                    addToAllItems(new Furnace(jsonValue, gameWorld), Color.CYAN);
                }
                break;
            case Constants.UPGRADER_FP:
                for (JsonValue jsonValue : fileContents) {
                    addToAllItems(new Upgrader(jsonValue, gameWorld), Color.BLUE);
                }
                break;
            case Constants.CONVEYORS_FP:
                for (JsonValue jsonValue : fileContents) {
                    addToAllItems(new Conveyor(jsonValue, gameWorld), Color.PURPLE);
                }
                break;
        }
    }

    private void addToAllItems(Item item, Color color) {
        if (allItems.containsKey(item.getID())) {
            throw new IllegalArgumentException("Conflicting IDs between " + item.getName() + " and " + allItems.get(item.getID()).getName() + ".");
        }
        allItems.put(item.getID(), item);
        loadCount++;
        Gdx.app.log(item.getClass().getSimpleName(), color.colorId + "Loaded " + item.getName() + Color.NONE.colorId);
    }

    private void mongoConnect() {
        //TODO: Add exception handling so that if you dont have internet it still works.
        Gdx.app.log("Mongo Loader", "Attempting to verify files...");
        Stopwatch stopwatch = new Stopwatch(TimeUnit.MILLISECONDS);
        stopwatch.start();
        try {
            MongoClient mongoClient = MongoClients.create("mongodb+srv://client:JAaTk8dtGkpSe42u@primarycluster.bonuplz.mongodb.net/");
            MongoDatabase database = mongoClient.getDatabase("OreForge");

            CompletableFuture<Void> conveyorFuture = CompletableFuture.runAsync(() -> checkVersion("Conveyors", Constants.CONVEYORS_FP, database));
            CompletableFuture<Void> upgraderFuture = CompletableFuture.runAsync(() -> checkVersion("Upgraders", Constants.UPGRADER_FP, database));
            CompletableFuture<Void> furnaceFuture = CompletableFuture.runAsync(() -> checkVersion("Furnaces", Constants.FURNACE_FP, database));
            CompletableFuture<Void> dropperFuture = CompletableFuture.runAsync(() -> checkVersion("Droppers", Constants.DROPPERS_FP, database));

            CompletableFuture.allOf(conveyorFuture, upgraderFuture, furnaceFuture, dropperFuture).join();//This forces all ansync tasks to be completed before returning from this function.
            mongoClient.close();
            stopwatch.stop();
            Gdx.app.log("Mongo Loader", Color.highlightString("Verified local files in " + stopwatch.getElapsedTime() + " ms.", Color.GREEN));
        } catch (Exception e) {
            Gdx.app.log("Mongo Loader", Color.highlightString("Failed to connect to MongoDB", Color.RED));
        }


    }

    private void checkVersion(String mongoCollection, String localFile, MongoDatabase database) {
        MongoCollection<Document> collection = database.getCollection(mongoCollection);
        // Project only the "version" field

        Document version = collection.find().first();
        JsonReader jsonReader = new JsonReader();
        JsonValue localFileContents;
        double localVersion = -1;//set value to -1 to ensure that it will never equal a mongoDB version by accident

        //Try to find the local document and its version. If it's not present we will automatically get
        //the most up-to-date version from MongoDB.
        try {
            localFileContents = jsonReader.parse(Gdx.files.local(localFile));
            localVersion = localFileContents.child().getDouble("version");
        } catch (Throwable ignored) {}

        double dbVersion = -2;
        try {
            dbVersion = version.getDouble("version");
        } catch (NullPointerException e) {
            throw new RuntimeException("Could not retrieve version from MongoDB collection: " + mongoCollection + "\n" + e);
        }

        //If the versions are out of sync we Overwrite the current version on the local
        //machine with the version from MongoDB.
        if (dbVersion != localVersion) {
            Gdx.app.log("Mongo Loader", Color.highlightString(localFile + "version " + localVersion + " did not align with" + mongoCollection + " collection version " + dbVersion, Color.YELLOW));
            downloadAndUpdate(localFile, collection);
        }
    }

    private void downloadAndUpdate(String localFile, MongoCollection<Document> collection) {
        FindIterable<Document> documents = collection.find();
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        FileHandle fileHandle = Gdx.files.local(localFile);
        long total = collection.countDocuments();
        long count = 0;
        StringBuilder jsonString = new StringBuilder("[\n");
        for (Document document : documents) {
            count++;
            document.remove("_id");//remove id from mongoDB
            jsonString.append(json.prettyPrint(document.toJson()));
            if (count < total) {//This block is used to ensure that a comma isn't placed after the final item in the JSON file.
                jsonString.append(",\n");
            } else {
                jsonString.append("\n");
            }
        }
        jsonString.append("]");
        fileHandle.writeString(jsonString.toString(),false);
    }

}
