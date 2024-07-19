package home.vcausality.launch;

import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkComputePipelineCreateInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

public class SingleCopy {
	private static final String CSH_NAME = "/home/vcausality/launch/csh02.spv";
	
	private long src;
	private List<Long> dst;
	
	private int swapchainSize;
	
	private long descriptorPool;
	private long descriptorSetLayout;
	private long pipeline;
	private long pipelineLayout;
	
	private static VkDevice device;
	private static VkPipelineShaderStageCreateInfo ssCreateInfo;
	private static ByteBuffer entryPoint;
	
	private List<Long> descriptorSets;
	
	public SingleCopy(long srcImageView, List<Long> dstImageViews, int swapchainSize) {
		this.src = srcImageView;
		this.dst = dstImageViews;
		this.swapchainSize = swapchainSize;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		dst.forEach(d -> sb.append(d).append(" "));
		StringBuilder sc = new StringBuilder();
		descriptorSets.forEach(d -> sc.append(d).append(" "));
		return "src: "+ src
				+ "\ndst: "+ sb.toString()
				+ "\nswapchainSize: "+ swapchainSize
				+ "\ndescriptorPool: "+ descriptorPool
				+ "\ndescriptorSetLayout: "+ pipeline
				+ "\npipeline: "+ pipeline
				+ "\npipelineLayout: "+ pipelineLayout
				+ "\ndescriptorSets: "+ sc.toString();
	}
	
	public static void createComputeShader(VkDevice d) {
		device = d;
		try(var stack = stackPush()){
			ByteBuffer csh = null;
			entryPoint = memUTF8("main");
			
			var cde0 = LaunchRenderer3.class.getResourceAsStream(CSH_NAME);
			if(cde0 == null) {
				throw new RuntimeException("Vertex shader has not found\n");
			}
			try {
				byte[] bf = cde0.readAllBytes();
				csh = stack.malloc(bf.length);
				csh.put(0, bf);
				cde0.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			var shCreateInfo = VkShaderModuleCreateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
					.pCode(csh);
			var pCsh = stack.mallocLong(1);
			if(vkCreateShaderModule(device, shCreateInfo, null, pCsh) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create single copy shader module\n");
			}
			ssCreateInfo = VkPipelineShaderStageCreateInfo.calloc()
					.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
					.stage(VK_SHADER_STAGE_COMPUTE_BIT)
					.module(pCsh.get(0))
					.pName(entryPoint);
		}
	}
	
	public SingleCopy createPipeline() {
		try(var stack = stackPush()) {
			var poolSizes = VkDescriptorPoolSize.calloc(2, stack);
			poolSizes.get(0)
					.type(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE) // Background, Texture or Glyph
					.descriptorCount(this.swapchainSize);
			poolSizes.get(1)
					.type(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE) // GBuffer
					.descriptorCount(this.swapchainSize);
		
			var poolInfo = VkDescriptorPoolCreateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
					.pPoolSizes(poolSizes)
					.maxSets(this.swapchainSize);
			var pDescriptorPool = stack.mallocLong(1);
			if(vkCreateDescriptorPool(device, poolInfo, null, pDescriptorPool)  != VK_SUCCESS) {
				throw new RuntimeException("Failed to create descriptor pool\n");
			}
			this.descriptorPool = pDescriptorPool.get(0);
			var binding = VkDescriptorSetLayoutBinding.calloc(2, stack);
			binding.get(0)
					.binding(0)
					.descriptorCount(1)
					.descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE)
					.stageFlags(VK_SHADER_STAGE_COMPUTE_BIT);
			binding.get(1)
					.binding(1)
					.descriptorCount(1)
					.descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE)
					.stageFlags(VK_SHADER_STAGE_COMPUTE_BIT);
				
			var createInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
					.pBindings(binding);
			var pDescriptorSetLayout = stack.mallocLong(this.swapchainSize);
			if(vkCreateDescriptorSetLayout(device, createInfo, null, pDescriptorSetLayout) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create compute descriptor set layout\n");
			}
			this.descriptorSetLayout = pDescriptorSetLayout.get(0);
			
			var layouts = stack.mallocLong(this.swapchainSize);
			for(int i = 0; i < layouts.capacity(); i++) {
				layouts.put(i, this.descriptorSetLayout);
			}
			var allocDSInfo = VkDescriptorSetAllocateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
					.descriptorPool(this.descriptorPool)
					.pSetLayouts(layouts);
			
			var pDescriptorSets = stack.mallocLong(this.swapchainSize);
			int result;
			if((result = vkAllocateDescriptorSets(device, allocDSInfo, pDescriptorSets)) != VK_SUCCESS) {
				throw new RuntimeException("Failed to allocate descriptor sets by: "+ result +"\n");
			}
			
			this.descriptorSets = new ArrayList<>(this.swapchainSize);
			var srciInfos = VkDescriptorImageInfo.calloc(1, stack);
			srciInfos.get(0)
					.imageLayout(VK_IMAGE_LAYOUT_GENERAL)
					.imageView(this.src);
			var dstiInfos = VkDescriptorImageInfo.calloc(1, stack);
			dstiInfos.get(0)
					.imageLayout(VK_IMAGE_LAYOUT_GENERAL);
			
			var descriptorWrites = VkWriteDescriptorSet.calloc(2, stack);
			descriptorWrites.get(0)
					.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
					.dstBinding(0)
					.dstArrayElement(0)
					.descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE)
					.descriptorCount(1);
			descriptorWrites.get(1)
					.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
					.dstBinding(1)
					.descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE)
					.descriptorCount(1);
			
			for(int i = 0; i < this.swapchainSize; i++) {
				dstiInfos.imageView(this.dst.get(i));
				long descriptorSet = pDescriptorSets.get(i);
				descriptorWrites.get(0)
						.dstSet(descriptorSet)
						.pImageInfo(srciInfos);
				descriptorWrites.get(1)
						.dstSet(descriptorSet)
						.pImageInfo(dstiInfos);
				
				vkUpdateDescriptorSets(device, descriptorWrites, null);
				this.descriptorSets.add(descriptorSet);
			}
			
			var range = VkPushConstantRange.calloc(1, stack);
			range.get(0)
					.stageFlags(VK_SHADER_STAGE_COMPUTE_BIT)
					.offset(0)
					.size(48 * Integer.BYTES);
			
			var plCreateInfo = VkPipelineLayoutCreateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
					.pSetLayouts(stack.longs(this.descriptorSetLayout))
					.pPushConstantRanges(range);
			var pPipelineLayout = stack.longs(VK_NULL_HANDLE);
			if(vkCreatePipelineLayout(device, plCreateInfo, null, pPipelineLayout) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create compute pipeline layout\n");
			}
			this.pipelineLayout = pPipelineLayout.get(0);
			var cpCreateInfo = VkComputePipelineCreateInfo.calloc(1, stack)
					.sType(VK_STRUCTURE_TYPE_COMPUTE_PIPELINE_CREATE_INFO)
					.layout(this.pipelineLayout)
					.stage(ssCreateInfo);
			var pPipeline = stack.mallocLong(1);
			if(vkCreateComputePipelines(device, VK_NULL_HANDLE, cpCreateInfo, null, pPipeline)
					!= VK_SUCCESS) {
				throw new RuntimeException("Failed to create compute pipeline\n");
			}
			this.pipeline = pPipeline.get(0);
			return this;
		}
	}
	
	public void drawSingle(VkCommandBuffer c, int imageIdx, ShaderUBOInfo si) {
		try(var stack = stackPush()){
			var dt = stack.malloc(ShaderUBOInfo.SIZE);
			ShaderUBOInfo.memcpy(dt, si);
			vkCmdPushConstants(c, this.getPipelineLayout(), VK_SHADER_STAGE_COMPUTE_BIT, 0, dt);
			vkCmdBindPipeline(c, VK_PIPELINE_BIND_POINT_COMPUTE, this.getPipeline());
			vkCmdBindDescriptorSets(
					c,
					VK_PIPELINE_BIND_POINT_COMPUTE,
					this.getPipelineLayout(),
					0,
					stack.longs(this.getDescriptorSet(imageIdx)),
					null);
			vkCmdDispatch(c,
					SingleCopy.calcDispatchSize(si.cpyRegion().x()),
					SingleCopy.calcDispatchSize(si.cpyRegion().y()),
					1);
		}
	}
	
	public static void freeAll() {
		vkDestroyShaderModule(device, ssCreateInfo.module(), null);
		ssCreateInfo.free();
		memFree(entryPoint);
	}
	
	public void freePipeline() {
		vkDestroyDescriptorPool(device, this.descriptorPool, null);
		vkDestroyDescriptorSetLayout(device, this.descriptorSetLayout, null);
		vkDestroyPipeline(device, this.pipeline, null);
		vkDestroyPipelineLayout(device, this.pipelineLayout, null);
	}
	
	public long getDescriptorSet(int idx) {
		return this.descriptorSets.get(idx);
	}
	
	public long getPipeline() {
		return this.pipeline;
	}
	
	public long getPipelineLayout() {
		return this.pipelineLayout;
	}
	
	public static int calcDispatchSize(int size) {
		return (size - 1 >> 5) + 1;
	}
}
