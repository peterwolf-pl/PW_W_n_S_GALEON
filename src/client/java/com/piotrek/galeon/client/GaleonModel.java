package com.piotrek.galeon.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 1:1 preview: 36m hull x 9.5m beam; 16 model pixels = 1 Minecraft metre.
 * The original curved-surface reference mesh is in model/ via the OBJ generator.
 * This in-game version stays within Minecraft's native ModelPart renderer.
 *
 * X = across beam, -Z = bow, +Z = stern, -Y = up.
 * Parent is offset +24px, matching the parent's sailboat renderer convention.
 */
public final class GaleonModel extends EntityModel<GaleonRenderState> {
    public static final int HULL_DARK=0, HULL=1, DECK=2, MAHOGANY=3, GOLD=4,
            GUN=5, CANVAS=6, ROPE=7, PORT=8, RED=9, GLASS=10, OAK=11,
            CREAM=12, COPPER=13, FLAG=14, IRON=15;
    private static int wireIndex;

    public GaleonModel(ModelPart root) { super(root); }

    private static final class Pieces {
        private final PartDefinition parent;
        private final Map<Integer, CubeListBuilder> byMaterial = new LinkedHashMap<>();
        Pieces(PartDefinition parent) { this.parent = parent; }
        void add(int material, float x,float y,float z,float w,float h,float d) {
            if(w <= 0 || h <= 0 || d <= 0) return;
            CubeListBuilder b=byMaterial.computeIfAbsent(material, m -> CubeListBuilder.create());
            b.texOffs((material%4)*512+8, (material/4)*512+8).addBox(x,y,z,w,h,d);
        }
        void flush() {
            for(var e:byMaterial.entrySet())
                parent.addOrReplaceChild("surface_"+e.getKey(),e.getValue(),PartPose.ZERO);
        }
    }

    /** A thin local-Z cuboid oriented from A to B. Split long rope into UV-safe segments. */
    private static void rig(PartDefinition parent, int material, String label,
                            float ax,float ay,float az,float bx,float by,float bz,float radius) {
        float dx=bx-ax, dy=by-ay, dz=bz-az;
        float length=(float)Math.sqrt(dx*dx+dy*dy+dz*dz);
        if(length < 0.01F) return;
        int steps=Math.max(1,(int)Math.ceil(length/220.0F));
        float ry=(float)Math.atan2(dx,dz);
        float rx=(float)Math.asin(-dy/length);
        for(int i=0;i<steps;i++) {
            float t=(float)i/steps;
            float sl=length/steps;
            parent.addOrReplaceChild(label+"_"+(wireIndex++),
                CubeListBuilder.create()
                    .texOffs((material%4)*512+8,(material/4)*512+8)
                    .addBox(-radius,-radius,0,radius*2,radius*2,sl),
                PartPose.offsetAndRotation(ax+dx*t,ay+dy*t,az+dz*t,rx,ry,0));
        }
    }

    private static void rail(Pieces p,float x,float y,float a,float b,int tone) {
        for(float z=a;z<b;z+=24) {
            p.add(MAHOGANY,x,y,z,2,28,3);
            p.add(tone,x,y-2,z,2,2,Math.min(24,b-z));
            p.add(GOLD,x,y+16,z,2,2,Math.min(24,b-z));
        }
    }

    private static void mast(Pieces p,float z,float bottom,float top,float diameter) {
        float h=bottom-top;
        p.add(MAHOGANY,-diameter/2,bottom-260,z-diameter/2,diameter,260,diameter);
        p.add(OAK,-diameter*0.43F,top+130,z-diameter*0.43F,diameter*.86F,
                h-390,diameter*.86F);
        p.add(OAK,-diameter*.32F,top,z-diameter*.32F,diameter*.64F,
                130,diameter*.64F);
        p.add(GOLD,-diameter*.5F,top-6,z-diameter*.5F,diameter,6,diameter);
        // Fighting top/platforms, common on early 17th-century square-rigged ships.
        p.add(DECK,-21,top+218,z-21,42,4,42);
        p.add(MAHOGANY,-23,top+212,z-23,46,5,2);
        p.add(MAHOGANY,-23,top+212,z+21,46,5,2);
    }

    private static void square(Pieces sails,Pieces fittings,float z,float top,float h,float w) {
        // Yard braces, two-colour folded cloth strips, horizontal seams and reef points.
        fittings.add(MAHOGANY,-w/2-9,top-5,z-3,w+18,5,6);
        fittings.add(GOLD,-w/2-10,top-5,z-3,5,6,7);
        fittings.add(GOLD,w/2+5,top-5,z-3,5,6,7);
        for(int row=0;row<6;row++) {
            float yy=top+row*h/6;
            float taper=5*(row/6f);
            sails.add(CANVAS,-w/2+taper,yy,z+5,w-2*taper,h/6+0.12f,2.0F);
            fittings.add(CREAM,-w/2+taper,yy,z+7,w-2*taper,0.75F,0.9F);
        }
        for(int k=0;k<5;k++) {
            float x=-w*.38f + k*w*.19f;
            fittings.add(ROPE,x,top+9,z+6,0.75F,h-13,1.0F);
        }
    }

    public static LayerDefinition createBodyLayer() {
        wireIndex=0;
        MeshDefinition mesh=new MeshDefinition();
        PartDefinition ship=mesh.getRoot().addOrReplaceChild(
                "galeon",CubeListBuilder.create(),PartPose.offset(0,24,0));
        Pieces p=new Pieces(ship);
        // Sixteen model pixels per metre; 576px long, up to 152px wide.
        float[] stations={-288,-260,-228,-190,-150,-105,-52,0,52,105,150,195,238,275,288};
        float[] halfW={3,23,40,57,68,74,76,76,75,72,68,65,61,58,55};
        float[] bandY={46,30,14,-2,-18,-34};
        float[] profile={.30f,.55f,.76f,.89f,1.0f,.93f};
        // Submerged rounded bilge, black/red wale and varied tumblehome at the gun deck.
        for(int i=0;i<stations.length-1;i++) {
            float z=stations[i], length=stations[i+1]-z+0.05F;
            float w=(halfW[i]+halfW[i+1])*.5f;
            for(int j=0;j<bandY.length;j++) {
                float hw=w*profile[j];
                int mat=j<2 ? HULL_DARK : j==3 ? RED : j==5 ? HULL : HULL;
                p.add(mat,-hw,bandY[j],z,hw*2,16,length);
                if(j>=2) {
                    p.add(MAHOGANY,-hw-1,bandY[j]+1,z,2,2,length);
                    p.add(MAHOGANY,hw-1,bandY[j]+1,z,2,2,length);
                }
            }
            // Keel & internal deck broken into station-width rectangles.
            p.add(DECK,-w*.84f,-51,z,w*1.68f,3,length);
            p.add(OAK,-w*.84f,-53,z,w*1.68f,.7f,length);
            p.add(HULL_DARK,-7,59,z,14,7,length);
        }
        // Waterline stripe and continuous upper bulwark/gunwale, interrupted gun ports.
        for(float z=-240;z<=228;z+=24) {
            float beam=75;
            if(z<-180)beam=52;
            else if(z>170)beam=63;
            p.add(RED,-beam-2,-18,z,3,3,22);
            p.add(RED,beam-1,-18,z,3,3,22);
            if(z > -190 && z < 195) {
                for(int side:new int[]{-1,1}) {
                    float x=side>0 ? beam-1 : -beam-4;
                    p.add(PORT,x,-43,z+6,5,20,20);
                    p.add(GOLD,x-.2f,-44,z+5,5.5f,2,22);
                    p.add(GOLD,x-.2f,-24,z+5,5.5f,2,22);
                    // Bronze gun with flared muzzle and carriage.
                    float barrelX=side>0 ? beam+1 : -beam-25;
                    p.add(GUN,barrelX,-34,z+13,24,7,7);
                    p.add(IRON,side>0?beam+20:-beam-27,-35,z+12,7,9,9);
                    p.add(MAHOGANY,side>0?beam-10:-beam+3,-30,z+9,12,7,14);
                }
            }
        }
        // Forecastle rises toward bow; teak deck and narrow beak with golden figurehead.
        p.add(DECK,-48,-81,-265,96,5,72);
        p.add(MAHOGANY,-48,-79,-264,5,28,69);
        p.add(MAHOGANY,43,-79,-264,5,28,69);
        p.add(GOLD,-10,-79,-290,20,4,20);
        p.add(GOLD,-6,-72,-304,12,12,18);
        p.add(MAHOGANY,-8,-61,-400,16,10,120); // 7m bowsprit projects ahead of bow.
        p.add(GOLD,-10,-60,-400,20,3,14);
        // Two lower sterncastle decks plus poop and upper balcony.
        for(int tier=0;tier<3;tier++) {
            float floor=-57-tier*29, from=177+tier*19, to=288;
            float width=69-tier*6;
            p.add(DECK,-width,floor,from,2*width,5,to-from);
            p.add(MAHOGANY,-width,floor-22,from,5,23,to-from);
            p.add(MAHOGANY,width-5,floor-22,from,5,23,to-from);
            p.add(MAHOGANY,-width,floor-26,to-5,2*width,27,6);
            p.add(GOLD,-width-2,floor-27,to-7,2*width+4,3,9);
            p.add(GOLD,-width-2,floor-4,to-7,2*width+4,3,9);
            // Rear glazed window grid and gilded surrounds.
            for(float x=-width+14;x<width-10;x+=19) {
                p.add(GOLD,x-2,floor-22,to+1,16,20,2);
                p.add(GLASS,x,floor-20,to+3,12,15,1.5F);
                p.add(GOLD,x,floor-13,to+4,12,1.5F,2);
            }
            for(int side:new int[]{-1,1}) {
                float x=side>0?width-7:-width-1;
                for(float z=from+10;z<to-15;z+=21) {
                    p.add(GOLD,x,floor-20,z,7,17,13);
                    p.add(GLASS,x+(side>0?6:-1),floor-18,z+2,2,12,9);
                }
            }
        }
        p.add(DECK,-42,-151,236,84,5,47);
        p.add(MAHOGANY,-44,-153,234,5,20,50);
        p.add(MAHOGANY,39,-153,234,5,20,50);
        p.add(GOLD,-48,-171,283,96,7,5); // stern crest/panel
        for(float x=-40;x<41;x+=13) {
            p.add(GOLD,x,-169,287,3,16,3);
        }
        // Prominent stern quarter galleries and layered teak balcony rails.
        for(int side:new int[]{-1,1}) {
            float x=side>0?67:-74;
            p.add(DECK,x,-83,221,9,5,67);
            p.add(DECK,x,-113,237,9,5,51);
            for(float z=232;z<=282;z+=12) {
                p.add(GOLD,x,-112,z,5,26,3);
            }
        }
        // Main deck hatch, gratings, companionways, capstan and ship's wheel.
        p.add(MAHOGANY,-24,-57,-62,48,4,64);
        for(float x=-21;x<22;x+=8) p.add(GUN,x,-58,-60,1.5f,2,60);
        for(float z=-60;z<4;z+=8) p.add(GUN,-22,-58,z,44,2,1.5f);
        p.add(OAK,-15,-65,72,30,12,18);
        p.add(MAHOGANY,-10,-75,78,20,11,8);
        p.add(GOLD,-5,-68,81,10,2,3);
        // Helm shaft and octagonal-looking wheel with spokes.
        p.add(MAHOGANY,-3,-76,157,6,32,8);
        p.add(GOLD,-5,-80,165,10,6,5);
        for(int spoke=-1;spoke<=1;spoke++) {
            p.add(MAHOGANY,-22,-92+spoke*12,172,44,3,3);
            p.add(MAHOGANY,-20+spoke*18,-108,172,3,34,3);
        }
        // Mast shafts, three square-rigged spars, two working tops and their caps.
        mast(p,-126,-52,-589,18);
        mast(p,24,-52,-690,22);
        mast(p,203,-85,-490,14);
        // Yards, reef seams, and unfurled cloth. Separate group for future sail animations.
        PartDefinition canvasPart=ship.addOrReplaceChild("sails",CubeListBuilder.create(),PartPose.ZERO);
        Pieces canvas=new Pieces(canvasPart);
        square(canvas,p,-126,-474,102,190);
        square(canvas,p,-126,-348,116,222);
        square(canvas,p,-126,-208,118,233);
        square(canvas,p,24,-558,108,185);
        square(canvas,p,24,-414,130,263);
        square(canvas,p,24,-261,145,302);
        // Mizzen lateen triangle with progressively widening trailing edge.
        p.add(MAHOGANY,-3,-395,190,6,6,175);
        for(int i=0;i<11;i++) {
            float yy=-391+i*24;
            float reach=16+i*13;
            canvas.add(CANVAS,-1,yy,206,2,23,reach);
            if(i%2==0) p.add(ROPE,-1,yy,206,2,1,reach);
        }
        // Spritsail on extended bowsprit.
        square(canvas,p,-345,-105,60,95);
        // Silk pennants at mastheads, high stern ensign.
        p.add(FLAG,2,-700,24,76,17,2);
        p.add(FLAG,2,-600,-126,66,16,2);
        p.add(FLAG,4,-502,203,50,15,2);
        p.add(MAHOGANY,0,-203,275,4,80,4);
        p.add(FLAG,4,-195,275,72,25,2);

        // Open stair rails & handrails, anchored in the rail profile.
        rail(p,-67,-81,-165,165,MAHOGANY);
        rail(p,65,-81,-165,165,MAHOGANY);
        rail(p,-52,-101,-260,-190,GOLD);
        rail(p,50,-101,-260,-190,GOLD);
        rail(p,-40,-172,238,283,GOLD);
        rail(p,38,-172,238,283,GOLD);

        // Mast standing rigging and fore/aft forestays, ropes are real angled beams.
        for(float[] m:new float[][]{{-126,-589},{24,-690},{203,-490}}) {
            float mz=m[0], tip=m[1];
            for(int side:new int[]{-1,1}) {
                for(int i=0;i<6;i++) {
                    float footZ=mz-45+i*18;
                    rig(ship,ROPE,"shroud",0,tip+220,mz,
                            side*69,-53,footZ,.65F);
                }
            }
        }
        rig(ship,ROPE,"forestay",0,-685,24,0,-77,-270,.95F);
        rig(ship,ROPE,"stay",0,-589,-126,0,-390,24,.86F);
        rig(ship,ROPE,"stay",0,-487,203,0,-371,24,.86F);
        rig(ship,ROPE,"jibstay",0,-489,-126,0,-67,-350,.85F);
        for(int side:new int[]{-1,1}) {
            rig(ship,ROPE,"bowspritstay",0,-72,-385,side*46,-69,-248,.7F);
        }
        // Horizontal ratlines between paired shrouds.
        for(int side:new int[]{-1,1}) {
            float x=side*54;
            for(int i=0;i<16;i++) {
                float y=-58-i*17, z=20+i*1.8f;
                rig(ship,ROPE,"ratline",x,y,z-32,x,y,z+32,.55F);
            }
        }
        // Fittings on weather decks: paired catheads, cleats, belaying rails.
        for(int side:new int[]{-1,1}) {
            for(float z:new float[]{-192,-105,10,118,201}) {
                float x=side>0?65:-73;
                p.add(MAHOGANY,x,-60,z,9,4,18);
                p.add(GOLD,x+2,-64,z+6,4,6,6);
            }
        }
        p.flush();
        canvas.flush();
        return LayerDefinition.create(mesh,2048,2048);
    }
}
