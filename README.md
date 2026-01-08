## What is Ore Forge?

In Ore Forge you assemble your tycoon out of 4 item types, each of which have tons of unique variants,
__*Droppers, Conveyors, Upgraders,*__ and __*Furnaces*__. Droppers generate/produce ore which is transported through
your
custom-built tycoon of conveyors and upgraders. As the ore moves through your tycoon each upgrader upgrades it,
increasing its value. Finally, the ore is delivered to a furnace where it is sold, often applying a bonus to the ore,
earning you cash.
This cash can be used purchase items from the shop or to prestige, granting you a random prestige tier item to use in
your tycoon.

---

## How to Install and Play

_**{INSERT HERE WHEN DONE}**_

---

## Controls

* **Camera Movement** - You can use `W` , `A` , `S`, `D` to move the camera up, left, down, and right respectively.
  Holding down `Shift` while
  pressing one of the movement keys doubles the speed of the camera.


* **Camera Zoom** - You can use `E` to zoom the camera inwards and `Q` to zoom the camera outwards.


* **Ore Observer Mode** - To lock the camera onto a specific ore and observe its stats you can press `V`. To cycle
  between other ore you can
  use `W` and `S`. To exit Ore Observer mode press `ESC`.

* **Selecting Items** - To select and item move your mouse over the item you wish to select then `Left-Click` on it, The
  item should now
  be highlighted green. From here you can pick up the item by pressing `R`, remove the item by pressing `Z`, {INSERT
  OTHER ACTIONS HERE}

* **Placing Items** - To place an item you can either press `Left-Click` or `SPACE`. Holding down either of these keys
  makes it so you don't have to press the button each time you want it to be placed, instead the item will automatically
  be placed if it's a valid position.
  While in build mode placing an item on top of another already placed item will remove it unless that item was placed
  during the current build mode session.

* **Shop and Inventory** - The shop and inventory can be accessed by clicking on their respective buttons/icons or by
  pressing their hotkeys.
  The inventory can be opened by pressing `F1` or `I`. The shop can be opened by pressing `F2` or `B`. To close these
  menus you can either press their respective hotkey, `ESC` or
  the X button/icon. To select an item from the shop simply click on the item you wish to select and it will either
  allow you to purchase more of that item
  or allow you to place that item.

* **Quest Tab** - The Quest Tab can be opened by pressing `J` or `F3`.

---

## Adding/Creating Your Own Custom Items

You can use [this](https://github.com/NathanUlmen/OreForge-Item-Json-Generator) tool to generate/create item data for
Ore Forge and to manage the items that you have created so far.

---

## Key Components:

* The [GameWorld](https://github.com/NathanUlmen/OreForge/blob/main/core/src/main/java/ore/forge/Screens/GameWorld.java)
  class is where the game is rendered and the game logic is driven from.
* Under
  the [Strategies Folder](https://github.com/NathanUlmen/OreForge/tree/main/core/src/main/java/ore/forge/Strategies) you
  can find the classes that are responsible upgrade logic and effect logic.
* The [Ore](https://github.com/NathanUlmen/OreForge/blob/main/core/src/main/java/ore/forge/Ore.java) class is pretty
  important as it's what the game revolves around.

---

## Libraries Used:

* [LibGDX](https://github.com/libgdx/libgdx)



main:
move left till null
move right till 1
    write 0
    move right till x
        goto copy
if read x:
    goto finalize

copy:
move right till 1
    goto write
if read =
    goto main


write:
replace 1 with 0
move right till null
write 1
move left till x
goto middle

middle:
move left till x
goto copy

finalize:
move left till null
move right till null:
    if read 0:
        write 1
write #
halt
