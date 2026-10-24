# Peterwolf's Wind & Sails: Galeon

**Fabric Minecraft 26.3 | First milestone: full-scale visual model**

Original early-seventeenth-century Spanish-style galeon, constructed at Minecraft world scale (1 block = 1 metre).

- 36m hull, 9.5m beam, approx. 4m draft.
- Fully geometric 3D reference mesh (model/galeon_master.obj, companion MTL).
- Native Java renderer in-game with multi-deck hull, fore and stern castles, port/starboard cannons, rigging, 3 masts, sails and helm.
- 2048x2048 pixel material atlas and item icon generated during build.
- Galeon placed from its spawn item in water at least five blocks deep.
- Requires the base Peterwolf's Wind & Sails mod.

**State:** static ship *model preview*, not yet a sailing or walkable ship. Later phases will implement true physics, interactive decks, rigging animation and navigation.

## Local commands

Place this repo beside pw_wind_n_sails in your java folder. Use Java 25.

    cd ~/Documents/java/PW_W_n_S_GALEON
    ../pw_wind_n_sails/gradlew -p . clean build

Regenerate high-detail reference OBJ with Node.js:

    node tools/generate_galeon_obj.mjs

To edit: import model/galeon_master.obj into Blender as Wavefront OBJ with its MTL file. Geometry is exported in real-world metres. The Minecraft Java model follows the same scale, using its own cube-friendly geometry.
