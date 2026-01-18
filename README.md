Load our Mesh using AssetManager into a SceneAsset

Construct our physics model from this scene asset for
our btCollisionDefinition.

------------------
How do we know when to evict stuff:
* When nothing references the assets && when we have more assets loaded then our threshold.
* Long time since last usage?
------------------

So on click to place items:
* Get our definition from icon
* Check to see if in cache :
    * if in cache continue to instantiating instance
* else :
    * asset manager async load visual model
    * create physics model from our visual model
