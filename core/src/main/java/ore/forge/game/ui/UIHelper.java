package ore.forge.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

import java.util.HashMap;

public class UIHelper {
    @SuppressWarnings("GDXJavaStaticResource")
    private final static FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Fonts/NotoSans-Bold.ttf"));
    private final static HashMap<Integer, BitmapFont> fontLookup = new HashMap<>();

    public static BitmapFont generateFont(int size) {
//        if (fontLookup.containsKey(size)) {
//            return fontLookup.get(size);
//        }
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.genMipMaps = true;
        parameter.minFilter = Texture.TextureFilter.MipMapLinearLinear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        var font = generator.generateFont(parameter);
        fontLookup.put(size, font);
        return font;
    }

    @SuppressWarnings("GDXJavaStaticResource")
    private final static Skin buttonTextures = new Skin(new TextureAtlas(Gdx.files.internal("UIAssets/UIButtons.atlas")));
    private final static HashMap<String, NinePatchDrawable> buttonLookup = new HashMap<>();


    public static NinePatchDrawable getRoundFull() {
        if (buttonLookup.containsKey(ButtonType.ROUND_FULL_128.getName())) {
            return buttonLookup.get(ButtonType.ROUND_FULL_128.getName());
        }
        var roundFull = new NinePatchDrawable(buttonTextures.getPatch("128xRoundFull"));
        buttonLookup.put(ButtonType.ROUND_FULL_128.getName(), roundFull);
        return roundFull;
    }

    public static NinePatchDrawable getButton(ButtonType type) {
        if (buttonLookup.containsKey(type.getName())) {
            return buttonLookup.get(type.getName());
        }
        var newNinePatch = new NinePatchDrawable(buttonTextures.getPatch(type.getName()));
        buttonLookup.put(type.getName(), newNinePatch);
        return newNinePatch;
    }

    @SuppressWarnings("GDXJavaStaticResource")
    private final static Skin icons = new Skin(new TextureAtlas(Gdx.files.internal("UIAssets/Icons.atlas")));
    private final static HashMap<String, NinePatchDrawable> iconLookup = new HashMap<>();


    public static NinePatchDrawable getIcon(String name) {
        if (iconLookup.containsKey(name)) {
            return iconLookup.get(name);
        }
        var newNinePatch = new NinePatchDrawable(icons.getPatch(name));
        iconLookup.put(name, newNinePatch);
        return newNinePatch;
    }

    @SuppressWarnings("GDXJavaStaticResource")
    private final static Skin glowSquares = new Skin(new TextureAtlas(Gdx.files.internal("UIAssets/GlowSquare.atlas")));
    private final static HashMap<String, NinePatchDrawable> glowSquareLookup = new HashMap<>();

    public static NinePatchDrawable getGlowSquare(GlowSquare glowSquare) {
        if (glowSquareLookup.containsKey(glowSquare.getName())) {
            return glowSquareLookup.get(glowSquare.getName());
        }
        var newNinePatch = new NinePatchDrawable(glowSquares.getPatch(glowSquare.getName()));
        iconLookup.put(glowSquare.getName(), newNinePatch);
        return newNinePatch;
    }

    public static List.ListStyle getListStyle() {
        var listStyle = new List.ListStyle();
        listStyle.background = UIHelper.getRoundFull();
        listStyle.selection = UIHelper.getButton(ButtonType.ROUND_EMPTY_128).tint(Color.YELLOW);
        listStyle.font = generateFont(16);
        listStyle.font.setColor(Color.BLACK);
        return listStyle;
    }

    public static NinePatchDrawable createBorder(int thickness, Color borderColor, Color fillColor) {
        Pixmap pixmap = new Pixmap(thickness, thickness, Pixmap.Format.RGBA8888);

        pixmap.setColor(fillColor);
        pixmap.fill();

        pixmap.setColor(borderColor);
        pixmap.drawRectangle(0, 0, thickness, thickness);

        return new NinePatchDrawable(new NinePatch(new Texture(pixmap), 10, 10, 10, 10));
    }

}
