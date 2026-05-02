package ore.forge.engine.importing;

import ore.forge.engine.VertexAttribute;

import java.nio.ByteBuffer;

/**
 * @param strideLength stride length in bytes
 */
public record AttributeHolder(VertexAttribute type, ByteBuffer buffer, int strideLength) {

}
