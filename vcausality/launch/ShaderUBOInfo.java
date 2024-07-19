package home.vcausality.launch;

import static org.lwjgl.system.MemoryUtil.*;

import java.nio.ByteBuffer;

import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

public class ShaderUBOInfo {
	public static final int BLEND_COLOR = 0;
	public static final int BLEND_DST = 1;
	public static final int COLOR_NONE = 0;
	public static final int SIZE = Integer.BYTES * 8;
	public static final int OFFSETOF_SRC = 0;
	public static final int OFFSETOF_DST = Integer.BYTES * 2;
	public static final int OFFSETOF_CPY = Integer.BYTES * 4;
	public static final int OFFSETOF_COLOR = Integer.BYTES * 6;
	public static final int OFFSETOF_BLEND = Integer.BYTES * 7;
	
	private ByteBuffer data;
	
	public ShaderUBOInfo(MemoryStack stack) {
		data = stack.calloc(SIZE);
	}
	
	public ShaderUBOInfo() {
		data = memCalloc(SIZE);
	}
	
	public ShaderUBOInfo srcCoord(int x, int y) {
		this.data.putInt(0, x);
		this.data.putInt(4, y);
		return this;
	}
	
	public ShaderUBOInfo dstCoord(int x, int y) {
		this.data.putInt(8, x);
		this.data.putInt(12, y);
		return this;
	}
	
	public ShaderUBOInfo cpyRegion(int w, int h) {
		this.data.putInt(16, w);
		this.data.putInt(20, h);
		return this;
	}
	
	public ShaderUBOInfo color(int rgba) {
		this.data.putInt(24, rgba);
		return this;
	}
	
	public ShaderUBOInfo blendConstant(int c) {
		this.data.putInt(28, c);
		return this;
	}
	
	public Vector2ic srcCoord() {
		return new Vector2i(data.getInt(0), data.getInt(4));
	}
	
	public Vector2ic dstCoord() {
		return new Vector2i(data.getInt(8), data.getInt(12));
	}
	
	public Vector2ic cpyRegion() {
		return new Vector2i(data.getInt(16), data.getInt(20));
	}
	
	public int color() {
		return this.data.getInt(24);
	}
	
	public int blendConstant() {
		return this.data.getInt(28);
	}
	
	void copy(VkCommandBuffer c, int imageIdx) {
		LaunchRenderer3.copy(c, imageIdx, this);
	}
	
	public static void memcpy(ByteBuffer dst, ShaderUBOInfo src) {
		dst.put(src.data);
		dst.rewind();
		src.data.rewind();
	}
	
	public void free() {
		memFree(this.data);
	}
}
