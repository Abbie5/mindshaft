package org.esotericist.mindshaft;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import java.lang.Math;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Arrays;
import java.util.Iterator;

class mindshaftScanner {

    Random random = new Random();

    private int layer = 0;

    private int startX = 0;
    private int startZ = 0;

    private float nextlayer = 0;

    private static long now = 0;
    private static int currentDim = 0;

    // fudge for player's current Y level
    private static final double fudgeY = 17 / 32D;

    // how many chunks can be cached per tick
    private static final int chunkCacheMax = 3;

    // minimum time in ticks before a chunk is considered stale
    private static final int expiry = 80;

    // random addition in ticks to expiry
    private static final int expiryFudge = 100;

    // minimum time in ticks before a chunk is forcibly removed
    // actual forced expiration time is forcedExpiry + expiry
    private static final int forcedExpiry = 800;

    // random addition in ticks to forcedExpiry
    private static final int forcedExpiryFudge = 1000;

    // default color for empty layers. dark green.
    private static final int defaultColor = 0x002200;

    private layerSegment emptyLayer = new layerSegment(defaultColor);

    static class chunkID {
        int dimension, x, z;

        @Override
        public int hashCode() {
            return Objects.hash(dimension, x, z);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof chunkID))
                return false;
            chunkID other = (chunkID) obj;
            return dimension == other.dimension && x == other.x && z == other.z;
        }

        public chunkID(int dim, int newX, int newZ) {
            dimension = dim;
            x = newX;
            z = newZ;
        }
    }

    static class layerSegment {
        private int[] color = new int[256];

        public layerSegment(Integer v) {
            Arrays.fill(color, v);
        }

        public void setColor(int x, int y, int c) {
            color[x + (y * 16)] = c;
        }

        public int getColor(int x, int y) {
            return color[x + (y * 16)];
        }
    }

    static class block {
        boolean solid = true;
        boolean intangible = false;
        boolean empty = false;
        boolean lit = false;
    }

    static class chunkData {
        long expiration = 0;
        block[][][] blockData = new block[16][256][16];
        boolean stale = false;
        boolean expired = false;
        LinkedHashMap<Integer, layerSegment> layers = new LinkedHashMap<Integer, layerSegment>(16, 0.75f, true);

        public chunkData() {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 256; j++) {
                    for (int k = 0; k < 16; k++) {
                        blockData[i][j][k] = new block();
                    }
                }
            }
        }
    }

    static class chunkCache extends LinkedHashMap<chunkID, chunkData> {
        protected boolean removeEldestEntry(Map.Entry<chunkID, chunkData> eldest) {
            chunkData thisChunk = eldest.getValue();
            if (thisChunk.expired || (thisChunk.stale && thisChunk.expiration <= mindshaftScanner.now)) {
                // Mindshaft.logger.info(now + ": removed chunk: " + eldest.getKey().x + ", " +
                // eldest.getKey().z);
                return true;
            } else {
                return false;
            }
        }

        public chunkCache(Integer i) {
            super();
        }
    }

    static chunkCache chunksKnown = new chunkCache(32);
    static LinkedList<chunkID> requestedChunks = new LinkedList<chunkID>();

    private int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    private boolean isLit(World world, BlockPos pos) {

        if (((world.getLightFor(EnumSkyBlock.BLOCK, pos) > 0)
                || (world.provider.isSurfaceWorld()) && (world.getLightFor(EnumSkyBlock.SKY, pos) > 0))) {
            return true;
        }
        return false;
    }

    private void requestChunk(chunkID chunk) {
        if (!requestedChunks.contains(chunk)) {
            // Mindshaft.logger.info(now+ ": requested chunk: " + chunk.x + ", " + chunk.z);
            requestedChunks.add(chunk);
        }
    }

    chunkData getChunk(chunkID chunk) {
        chunkData thischunk = chunksKnown.get(chunk);
        if (thischunk != null) {
            if (!thischunk.stale && thischunk.expiration <= now) {
                // Mindshaft.logger.info(now + ": stale chunk: " + chunk.x + ", " + chunk.z + ",
                // stale at:" + thischunk.expiration );
                thischunk.stale = true;
                thischunk.expiration = now + forcedExpiry + random.nextInt(forcedExpiryFudge);
                requestChunk(chunk);
            }
            // Mindshaft.logger.info(now + ": fetched chunk: " + chunk.x + ", " + chunk.z +
            // ", stale at:" + thischunk.expiration );
            return chunksKnown.get(chunk);
        } else {
            requestChunk(chunk);
        }
        return null;
    }

    int processColumn(World world, chunkID chunk, int x, int z, int pY) {
        int color = defaultColor;
        int red = 0;
        int blue = 0;
        int green = 0;

        int dist;

        chunkData thisChunk = chunksKnown.get(chunk);

        for (int y = -15; y < 17; y++) {
            int intensity = 0;
            dist = Math.abs(y);

            if( y + pY < 0 ) {
                green = green + 17;
                continue;
            }
            if( y + pY > 255  ) {
                blue = blue + 16;
                continue;
            }

            block thisBlock = thisChunk.blockData[x][y + pY ][z];

            if (y > 1) {
                dist--;
            }
            if (y == 1) {
                dist = 0;
            }
            if (dist > 10) {
                dist = 10;
            }

            if (!thisBlock.intangible || !thisBlock.lit) {
                if (dist > 0) {
                    intensity = (11 - dist);
                } else {
                    intensity = 17;
                }
                if (!thisBlock.solid && thisBlock.lit) {
                    intensity = intensity - 3;
                }
            }

            intensity = Math.max(intensity, 0);

            green = green + intensity;
            if (thisBlock.empty && thisBlock.lit) {
                if (y < 0) {
                    red = red + (15 - dist);
                }
                if (y > 1) {
                    blue = blue + (16 - dist);
                }
            }
        }

        color = clamp(red, 0, 255) << 16 | clamp(green, 0, 255) << 8 | clamp(blue, 0, 255);
        // Mindshaft.logger.info(
        //    "chunk xz:" + chunk.x + "," + chunk.z +
        //    " xyz:" + x + "," + pY + "," + z + " rgb:" + red + "," + green + "," + blue);
        return color;
    }

    layerSegment processSegment(World world, chunkID chunk, int pY) {
        layerSegment segment = new layerSegment(defaultColor);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {

                int c = processColumn(world, chunk, x, z, pY);
                segment.setColor(x, z, c);
            }
        }

        return segment;
    }


    layerSegment addLayerSegment(World world, chunkID chunk, int pY) {
        layerSegment segment = processSegment(world, chunk, pY);
        chunksKnown.get(chunk).layers.put(pY, segment);
        
        return segment;

        /*
        random.setSeed(chunk.x * chunk.z);
        layerSegment testSegment = new layerSegment(random.nextInt(0xFFFFFF));
        return testSegment;
        */
    }

    layerSegment getLayerSegment(World world, chunkID chunk, int pY) {
        chunkData thisChunk = getChunk(chunk);
        if (thisChunk == null) {
            return emptyLayer;
        }
        layerSegment thisSegment = chunksKnown.get(chunk).layers.get(pY);
        if (thisSegment == null) {
            thisSegment = addLayerSegment(world, chunk, pY);
        }

        return thisSegment;
    }


    block getBlock(World world, chunkID chunk, int x, int y, int z ) {
        BlockPos pos = new BlockPos( chunk.x * 16 + x, y, chunk.z * 16 + z );
        block thisBlock = new block();

        IBlockState state = world.getBlockState(pos);
        Block blockID = state.getBlock();

        thisBlock.lit = true;
        thisBlock.lit = isLit(world, pos);

        if (state.isOpaqueCube() != true) {
            thisBlock.solid = false;

            if (state.getCollisionBoundingBox(world, pos) == null) {
                thisBlock.intangible = true;

                if (blockID.isAir(state, world, pos)) {
                    thisBlock.empty = true;
                }
            }
        }

        return thisBlock;
    }

    void scanChunk(World world, chunkID chunk) {
        chunkData newChunk = new chunkData();
        newChunk.expiration = now + expiry + random.nextInt(expiryFudge);
        // Mindshaft.logger.info(now + ": new chunk: " + chunk.x + ", " + chunk.z + ",
        // stale at: " + newChunk.expiration);
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 256; y++) {
                for (int z = 0; z < 16; z++) {
                    newChunk.blockData[x][y][z] = getBlock(world, chunk, x, y, z);
                }
            }
        }

        chunksKnown.put(chunk, newChunk);
    }

    void copyLayer(mindshaftRenderer renderer, layerSegment segment, int cX, int cZ) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int col = segment.getColor(x, z);
                renderer.setTextureValue((cX * 16) + x, (cZ * 16) + z, col);
            }
        }
    }

    public void rasterizeLayers(World world, EntityPlayer player, mindshaftRenderer renderer, zoomState zoom) {
        int pcX = (int) Math.floor(player.posX / 16.0) - 8;
        int pcZ = (int) Math.floor(player.posZ / 16.0) - 8;

        for (int cX = 0; cX < 16; cX++) {
            for (int cZ = 0; cZ < 16; cZ++) {
                chunkID thisChunk = new chunkID(currentDim, cX + pcX, cZ + pcZ);
                layerSegment thisSegment = getLayerSegment(world, thisChunk, (int) (player.posY - fudgeY));
                // Mindshaft.logger.info("cX: " + cX + ", cZ: " + cZ + "");
                copyLayer(renderer, thisSegment, cX, cZ);
            }
        }
        renderer.refreshTexture();
    }

    public void processChunks(World world) {
        now = world.getTotalWorldTime();
        currentDim = world.provider.getDimension();
        if (!requestedChunks.isEmpty()) {
            int cacheCount = 0;
            Iterator<chunkID> itr = requestedChunks.iterator();
            while (itr.hasNext() && cacheCount++ <= chunkCacheMax) {
                scanChunk(world, itr.next());
                itr.remove();
            }
        }
        if (! chunksKnown.isEmpty()) {
            int removeCount = 0;
            Set<Map.Entry<chunkID,chunkData>> entryset = chunksKnown.entrySet();
            Iterator<Map.Entry<chunkID,chunkData>> itr = entryset.iterator();
            while( itr.hasNext() && removeCount++ <= chunkCacheMax) {
                Map.Entry<chunkID,chunkData> entry = itr.next();
                chunkData chunk = entry.getValue();
                if( chunk.stale && chunk.expiration >= now ) {
                    itr.remove();
                }
            }
            
            
        }
    }
}