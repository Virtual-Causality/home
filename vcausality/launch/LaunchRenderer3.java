package home.vcausality.launch;

import static java.util.stream.Collectors.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.*;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.stb.STBImageWrite.*;
import static org.lwjgl.system.Configuration.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkExtent3D;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageSubresourceLayers;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkLayerProperties;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkOffset3D;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSamplerCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkViewport;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

public class LaunchRenderer3 {
	
	static class Vertex {
		static final int SIZEOF = 4 * Float.BYTES;
		static final int OFFSET_POS = 0;
		static final int OFFSET_TEXCOORDS = 2 * Float.BYTES;
		Vector2fc pos;
		Vector2fc texCoords;
		
		Vertex(Vector2fc pos, Vector2fc texCoords) {
			this.pos = pos;
			this.texCoords = texCoords;
		}
		
		static VkVertexInputBindingDescription.Buffer getBindingDescription(MemoryStack stack){
			return VkVertexInputBindingDescription.calloc(1, stack)
					.binding(0)
					.stride(SIZEOF)
					.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);
		}
		
		static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions(MemoryStack stack){
			var buffer = VkVertexInputAttributeDescription.calloc(2, stack);
			buffer.get(0)
					.binding(0)
					.location(0)
					.format(VK_FORMAT_R32G32_SFLOAT)
					.offset(OFFSET_POS);
			buffer.get(1)
					.binding(0)
					.location(1)
					.format(VK_FORMAT_R32G32_SFLOAT)
					.offset(OFFSET_TEXCOORDS);
			return buffer.rewind();
		}
		
		static void memcpy(ByteBuffer buffer, Vertex[] vertices) {
			for(Vertex v : vertices) {
				buffer.putFloat(v.pos.x());
				buffer.putFloat(v.pos.y());
				buffer.putFloat(v.texCoords.x());
				buffer.putFloat(v.texCoords.y());
			}
			buffer.rewind();
		}
		
		static void memcpy(ByteBuffer buffer, short[] indices) {
			for(short s : indices) {
				buffer.putShort(s);
			}
			buffer.rewind();
		}
		
		static final Vertex[] FSCR = {
				new Vertex(new Vector2f(-1.0f, -1.0f), new Vector2f(0.0f, 0.0f)),
				new Vertex(new Vector2f(1.0f, -1.0f), new Vector2f(0.78125f, 0.0f)),
				new Vertex(new Vector2f(1.0f, 1.0f), new Vector2f(0.78125f, 0.87890625f)),
				new Vertex(new Vector2f(-1.0f, 1.0f), new Vector2f(0.0f, 0.87890625f))
		};
		
		static final short[] INDICES = {
				0, 1, 2,
				2, 3, 0
		};
	}
	
	static class Frame {
		final long imageAvailableSemaphore;
		final long renderFinishedSemaphore;
		final long fence;
		VkCommandBuffer cb;
		
		Frame(long imageAvailableSemaphore, long renderFinishedSemaphore, long fence){
			this.imageAvailableSemaphore = imageAvailableSemaphore;
			this.renderFinishedSemaphore = renderFinishedSemaphore;
			this.fence = fence;
		}
	}
	
	static class Image {
		int height;
		int width;
		int format;
		int tiling;
		int usage;
		long sampler;
		long view;
		long image;
		long memory;
		
		static void free(VkDevice d, Image i) {
			vkDestroySampler(d, i.sampler, null);
			vkDestroyImageView(d, i.view, null);
			vkDestroyImage(d, i.image, null);
			vkFreeMemory(d, i.memory, null);
		}
	}
	
	static class Buffer {
		int offset;
		int length;
		long buffer;
		long memory;
		
		static void free(VkDevice d, Buffer b) {
			vkDestroyBuffer(d, b.buffer, null);
			vkFreeMemory(d, b.memory, null);
		}
	}
	
	private static long window;
	private static long surface;
	private static long debugMessenger;
	private static long swapchain;
	private static long computeCommandPool;
	private static long descriptorPool;
	private static long descriptorSetLayout;
	private static long pipelineLayout;
	private static long renderPass;
	private static long graphicsPipeline;
	private static long vertexBuffer;
	private static long vertexBufferMemory;
	private static long indexBuffer;
	private static long indexBufferMemory;

	private static int computeQueueIdx;
	private static int swapchainImageFormat;
	
	private static Image textureImage;
	private static Image backgroundImage;
	private static List<Image> gBufferImages;
	
	private static Buffer cellPositionsBuffer;
	private static Buffer segmentPositionsBuffer;
	private static Buffer guidePositionsBuffer;
	private static Buffer controlPositionsBuffer;
	private static Buffer screenShotBuffer;
	private static List<Buffer> cellStatesBuffers;
	private static List<Buffer> segmentStatesBuffers;
	private static List<Buffer> guideStatesBuffers;
	private static List<Buffer> controlStatesBuffers;
	
	private static SingleCopy bgShader;
	private static SingleCopy cpyShader;
	private static ASCII8x20Copy strShader;
	private static CellCopy cellShader;
	private static ControlCopy ctrlShader;
	private static GuideCopy guideShader;
	private static SegmentCopy segmentShader;
	private static ColorCopy colorShader;

	private static List<Long> swapchainFramebuffers;
	private static List<Long> swapchainImages;
	private static List<Long> swapchainImageViews;
	private static List<Long> descriptorSets;
	
	private static List<Frame> inFlightFrames;
	private static Map<Integer, Frame> imagesInFlight;
	private static int currentFrame;

	private static final boolean ENABLE_VALIDATION_LAYERS = DEBUG.get(true);
	private static final Set<String> VALIDATION_LAYERS;
	static {
		if(ENABLE_VALIDATION_LAYERS) {
			VALIDATION_LAYERS = new HashSet<>();
			VALIDATION_LAYERS.add("VK_LAYER_KHRONOS_validation");
		} else {
			VALIDATION_LAYERS = null;
		}
	}
	private static final Set<String> DEVICE_EXTENSIONS = Stream.of(VK_KHR_SWAPCHAIN_EXTENSION_NAME)
			.collect(toSet());
	private static VkInstance instance;
	private static VkPhysicalDevice pDevice;
	private static VkDevice device;
	private static VkQueue computeQueue;
	private static VkExtent2D swapchainExtent;
	private static VkPipelineShaderStageCreateInfo.Buffer prtUI;
	private static ByteBuffer shEPName;
	private static VkPhysicalDeviceMemoryProperties memProps;
	
	private static String GPUName;
	private static String APIVersion;
	private static String driverVersion;
	// private static final String TEXTURE_NAME = "home/vcausality/launch/shaft_tex_3.png";
	// private static final String BACKGROUND_NAME = "home/vcausality/launch/backgroundx.png";
	private static final String VSH_NAME = "/home/vcausality/launch/vsh01.spv";
	private static final String FSH_NAME = "/home/vcausality/launch/fsh01.spv";
	private static final long UINT64_MAX = 0xFFFFFFFFFFFFFFFFL;
	private static final int UINT32_MAX = 0xFFFFFFFF;
	private static final int MAX_FRAMES_IN_FLIGHT = 2;
	
	private static int cw = 1600;
	private static int ch = 1800;
	
	private static boolean requestedResize = false;
	private static boolean requestedScreenShot = false;
	private static int screenShotDrew = -1;
	private static boolean gameShouldContinue = true;
	private static boolean requestedVSync = true;
	private static boolean vsync = true;
	
	public static void main(String[] args) {
		init();
		while(gameShouldContinue) {
			gameShouldContinue &= !glfwWindowShouldClose(window);
			glfwPollEvents();
			drawFrame();
		}
		vkDeviceWaitIdle(device);
		cleanup();
	}
	
	static void requestGameClose() {
		gameShouldContinue = false;
	}
	
	private static void init() {
		Launch.initArrays();
		createDevice();
		createTextureImage();
		createFixedPositionsBuffers();
		createDescriptorSetLayout();
		createShaderStage();
		createSwapchainObjects(cw, ch);
		createSyncObjects();
	}
	
	private static void createDevice() {
		window = Launch.initSystem();
		if(ENABLE_VALIDATION_LAYERS && !checkValidationLayerSupport()) {
			throw new RuntimeException("Validation requested but not supported\n");
		}
		try(var stack = stackPush()){
			var createInfo = VkInstanceCreateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
					.pApplicationInfo(VkApplicationInfo.calloc(stack)
							.sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
							.pApplicationName(stack.UTF8Safe("test"))
							.applicationVersion(VK_MAKE_VERSION(1, 0, 0))
							.apiVersion(VK_API_VERSION_1_0))
					.ppEnabledExtensionNames(getRequiredExtensions(stack));
			if(ENABLE_VALIDATION_LAYERS) {
				createInfo.ppEnabledLayerNames(asPointerBuffer(stack, VALIDATION_LAYERS))
						.pNext(populateDebugMessengerCreateInfo(stack).address());
			}
			var pInstance = stack.mallocPointer(1);
			if(vkCreateInstance(createInfo, null, pInstance) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create instance\n");
			}
			instance = new VkInstance(pInstance.get(0), createInfo);
			
			if(ENABLE_VALIDATION_LAYERS) {
				var pDebugMessenger = stack.longs(VK_NULL_HANDLE);
				var dCreateInfo = populateDebugMessengerCreateInfo(stack);
				int result = VK_ERROR_EXTENSION_NOT_PRESENT;
				if(vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT") != NULL) {
					result = vkCreateDebugUtilsMessengerEXT(instance, dCreateInfo, null, pDebugMessenger);
				}
				if(result != VK_SUCCESS) {
					throw new RuntimeException("Failed to set up debug messenger\n");
				}
				debugMessenger = pDebugMessenger.get(0);
			}
			
			var pSurface = stack.longs(VK_NULL_HANDLE);
			if(glfwCreateWindowSurface(instance, window, null, pSurface) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create window surface\n");
			}
			surface = pSurface.get(0);
			
			var deviceCount = stack.ints(0);
			vkEnumeratePhysicalDevices(instance, deviceCount, null);
			if(deviceCount.get(0) == 0) {
				throw new RuntimeException("Failed to find GPU with Vulkan support\n");
			}
			var pPhysicalDevice = stack.mallocPointer(deviceCount.get(0));
			var props = VkPhysicalDeviceProperties.malloc(stack);
			vkEnumeratePhysicalDevices(instance, deviceCount, pPhysicalDevice);
			
			pDevice = new VkPhysicalDevice(pPhysicalDevice.get(0), instance);
			
			var extensionCount = stack.ints(0);
			vkEnumerateDeviceExtensionProperties(pDevice, (String)null, extensionCount, null);
			var availableExtensions = VkExtensionProperties.malloc(extensionCount.get(0));
			vkEnumerateDeviceExtensionProperties(pDevice, (String)null, extensionCount, availableExtensions);
			boolean containsAll = availableExtensions.stream()
					.map(VkExtensionProperties::extensionNameString)
					.collect(toSet())
					.containsAll(DEVICE_EXTENSIONS);
			if(!containsAll) {
				throw new RuntimeException("Swapchain extension unavailable\n");
			}
			
			if(pDevice == null) {
				throw new RuntimeException("Failed to find a suitable GPU\n");
			}
			vkGetPhysicalDeviceProperties(pDevice, props);
			GPUName = props.deviceNameString();
			int v = props.apiVersion();
			APIVersion = String.format("%d.%d.%d",
					VK_API_VERSION_MAJOR(v), VK_API_VERSION_MINOR(v), VK_VERSION_PATCH(v));
			v = props.driverVersion();
			driverVersion = String.format("0x%x", v);
			//System.out.println(DriverVersion);
			
			memProps = VkPhysicalDeviceMemoryProperties.malloc();
			vkGetPhysicalDeviceMemoryProperties(pDevice, memProps);
			
			var queueFamilyCount = stack.ints(0);
			var presentSupport = stack.ints(VK_FALSE);
			
			computeQueueIdx = -1;
			vkGetPhysicalDeviceQueueFamilyProperties(pDevice, queueFamilyCount, null);
			var queueFamilies = VkQueueFamilyProperties.malloc(queueFamilyCount.get(0), stack);
			vkGetPhysicalDeviceQueueFamilyProperties(pDevice, queueFamilyCount, queueFamilies);
			int cFlg = VK_QUEUE_GRAPHICS_BIT | VK_QUEUE_COMPUTE_BIT | VK_QUEUE_TRANSFER_BIT;
			for(int i = 0; i < queueFamilies.capacity(); i++) {
				if((queueFamilies.get(i).queueFlags() & cFlg) == cFlg) {
					presentSupport.put(0, VK_FALSE);
					vkGetPhysicalDeviceSurfaceSupportKHR(pDevice, i, surface, presentSupport);
					if(presentSupport.get(0) == VK_TRUE) {
						computeQueueIdx = i;
					}
				}
			}
			if(computeQueueIdx == -1) {
				throw new RuntimeException("No one supports Presentation\n");
			}
			var queueCreateInfos = VkDeviceQueueCreateInfo.calloc(1, stack)
					.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
					.queueFamilyIndex(computeQueueIdx)
					.pQueuePriorities(stack.floats(1.0f));
			var deviceFeatures = VkPhysicalDeviceFeatures.calloc(stack);
			vkGetPhysicalDeviceFeatures(pDevice, deviceFeatures);
			var deviceCreateInfo = VkDeviceCreateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
					.pQueueCreateInfos(queueCreateInfos)
					.pEnabledFeatures(deviceFeatures)
					.ppEnabledExtensionNames(asPointerBuffer(stack, DEVICE_EXTENSIONS));
			if(ENABLE_VALIDATION_LAYERS) {
					deviceCreateInfo.ppEnabledLayerNames(asPointerBuffer(stack, VALIDATION_LAYERS));
			}
			var qDevice = stack.pointers(VK_NULL_HANDLE);
			if(vkCreateDevice(pDevice, deviceCreateInfo, null, qDevice) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create logical device\n");
			}
			device = new VkDevice(qDevice.get(0), pDevice, deviceCreateInfo);
			
			var pGraphicsQueue = stack.pointers(VK_NULL_HANDLE);
			vkGetDeviceQueue(device, computeQueueIdx, 0, pGraphicsQueue);
			computeQueue = new VkQueue(pGraphicsQueue.get(0), device);
			var poolInfo = VkCommandPoolCreateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
					.flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT)
					.queueFamilyIndex(computeQueueIdx);
			var pCommandPool = stack.mallocLong(1);
			if(vkCreateCommandPool(device, poolInfo, null, pCommandPool) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create graphics command pool\n");
			}
			computeCommandPool = pCommandPool.get(0);
			
			long vbSize = Vertex.SIZEOF * Vertex.FSCR.length;
			var pVBuffer = stack.mallocLong(1);
			var pVBufferMemory = stack.mallocLong(1);
			createBuffer(vbSize,
					VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
					VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
					pVBuffer,
					pVBufferMemory);
			long sVBuffer = pVBuffer.get(0);
			long sVBufferMemory = pVBufferMemory.get(0);
			
			var data = stack.mallocPointer(1);
			vkMapMemory(device, sVBufferMemory, 0, vbSize, 0, data);
			Vertex.memcpy(data.getByteBuffer(0, (int)vbSize), Vertex.FSCR);
			vkUnmapMemory(device, sVBufferMemory);
			
			createBuffer(vbSize,
					VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
					VK_MEMORY_HEAP_DEVICE_LOCAL_BIT,
					pVBuffer,
					pVBufferMemory);
			
			vertexBuffer = pVBuffer.get(0);
			vertexBufferMemory = pVBufferMemory.get(0);
			
			copyBuffer(sVBuffer, vertexBuffer, vbSize);
			
			vkDestroyBuffer(device, sVBuffer, null);
			vkFreeMemory(device, sVBufferMemory, null);
			
			long ibSize = Short.BYTES * Vertex.INDICES.length;
			var pIBuffer = stack.mallocLong(1);
			var pIBufferMemory = stack.mallocLong(1);
			createBuffer(ibSize,
					VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
					VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
					pIBuffer,
					pIBufferMemory);
			long sIBuffer = pIBuffer.get(0);
			long sIBufferMemory = pIBufferMemory.get(0);
			
			var datb = stack.mallocPointer(1);
			vkMapMemory(device, sIBufferMemory, 0, ibSize, 0, datb);
			Vertex.memcpy(datb.getByteBuffer(0, (int)ibSize), Vertex.INDICES);
			vkUnmapMemory(device, sIBufferMemory);
			
			createBuffer(ibSize,
					VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
					VK_MEMORY_HEAP_DEVICE_LOCAL_BIT,
					pIBuffer,
					pIBufferMemory);
			
			indexBuffer = pIBuffer.get(0);
			indexBufferMemory = pIBufferMemory.get(0);
			
			copyBuffer(sIBuffer, indexBuffer, ibSize);
			
			vkDestroyBuffer(device, sIBuffer, null);
			vkFreeMemory(device, sIBufferMemory, null);
			
			SingleCopy.createComputeShader(device);
			ASCII8x20Copy.createComputeShader(device);
			CellCopy.createComputeShader(device);
			ControlCopy.createComputeShader(device);
			GuideCopy.createComputeShader(device);
			SegmentCopy.createComputeShader(device);
			ColorCopy.createComputeShader(device);
		}
	}
	
	static void background(VkCommandBuffer c, int i, ShaderUBOInfo u) {
		bgShader.drawSingle(c, i, u);
	}
	
	static void copy(VkCommandBuffer c, int i, ShaderUBOInfo u) {
		cpyShader.drawSingle(c, i, u);
	}
	
	static void ascii(VkCommandBuffer c, int i, int x, int y, String m) {
		strShader.drawASCII8x20(c, i, x, y, m);
	}
	
	static void cells(VkCommandBuffer c, int i) {
		cellShader.drawCells(c, i);
	}
	
	static void control(VkCommandBuffer c, int i) {
		ctrlShader.drawControl(c, i);
	}
	
	static void guide(VkCommandBuffer c, int i) {
		guideShader.drawGuide(c, i);
	}
	
	static void segment(VkCommandBuffer c, int i) {
		segmentShader.drawSegment(c, i);
	}
	
	static void rect(VkCommandBuffer c, int i, ShaderUBOInfo u) {
		colorShader.drawColor(c, i, u);
	}
	
	static String getGPUName() {
		return GPUName;
	}
	
	static String getAPIVersion() {
		return APIVersion;
	}
	
	static String getDriverVersion() {
		return driverVersion;
	}
	
	static void requestScreenShot() {
		requestedScreenShot = true;
	}
	
	static void requestVSync(boolean v) {
		requestedVSync = true;
		vsync = v;
	}
	
	private static void createSwapchainObjects(int width, int height) {
		createSwapchain(width, height);
		createGBufferImages();
		createUniformBuffers();
		createGraphicsPipeline();
		createFramebuffers();
		createDescriptorPool();
		createDescriptorSets();
	}
	
	private static void createSwapchain(int width, int height) {
		try(var stack = stackPush()){
			var capabilities = VkSurfaceCapabilitiesKHR.malloc(stack);
			vkGetPhysicalDeviceSurfaceCapabilitiesKHR(pDevice, surface, capabilities);
			var cnt = stack.ints(0);
			vkGetPhysicalDeviceSurfaceFormatsKHR(pDevice, surface, cnt, null);
			if(cnt.get(0) == 0) {
				throw new RuntimeException("Surface format is not found\n");
			}
			var formats = VkSurfaceFormatKHR.malloc(cnt.get(0), stack);
			vkGetPhysicalDeviceSurfaceFormatsKHR(pDevice, surface, cnt, formats);
			vkGetPhysicalDeviceSurfacePresentModesKHR(pDevice, surface, cnt, null);
			if(cnt.get(0) == 0) {
				throw new RuntimeException("Present mode is not found\n");
			}
			var presentModes = stack.mallocInt(cnt.get(0));
			vkGetPhysicalDeviceSurfacePresentModesKHR(pDevice, surface, cnt, presentModes);
			int presentMode = VK_PRESENT_MODE_FIFO_KHR;
			for(int i = 0; i < presentModes.capacity(); i++) {
				if(!vsync && presentModes.get(i) == VK_PRESENT_MODE_IMMEDIATE_KHR) {
					presentMode = VK_PRESENT_MODE_IMMEDIATE_KHR;
					break;
				}
			}
			var format = formats.stream()
					.filter(a -> a.format() == VK_FORMAT_B8G8R8A8_UNORM)
					.filter(a -> a.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
					.findAny()
					.orElse(formats.get(0));
			var extent = VkExtent2D.malloc(stack);
			if(capabilities.currentExtent().width() != UINT32_MAX) {
				extent.set(capabilities.currentExtent());
			} else {
				var min = capabilities.minImageExtent();
				var max = capabilities.maxImageExtent();
				extent.set(width, height)
						.width(Math.max(min.width(), Math.min(max.width(), extent.width())))
						.height(Math.max(min.height(), Math.min(max.width(), extent.height())));
			}
			var imageCount = stack.ints(capabilities.minImageCount() + 1);
			if(capabilities.maxImageCount() > 0 && imageCount.get(0) > capabilities.maxImageCount()) {
				imageCount.put(0, capabilities.maxImageCount());
			}
			var createInfo = VkSwapchainCreateInfoKHR.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
					.surface(surface)
					.minImageCount(imageCount.get(0))
					.imageFormat(format.format())
					.imageColorSpace(format.colorSpace())
					.imageExtent(extent)
					.imageArrayLayers(1)
					.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT)
					.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE)
					.preTransform(capabilities.currentTransform())
					.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
					.presentMode(presentMode)
					.clipped(true)
					.oldSwapchain(VK_NULL_HANDLE);
			var pSwapchain = stack.longs(VK_NULL_HANDLE);
			if(vkCreateSwapchainKHR(device, createInfo, null, pSwapchain) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create swapchain\n");
			}
			swapchain = pSwapchain.get(0);
			
			vkGetSwapchainImagesKHR(device, swapchain, imageCount, null);
			var pSwapchainImages = stack.mallocLong(imageCount.get(0));
			vkGetSwapchainImagesKHR(device, swapchain, imageCount, pSwapchainImages);
			swapchainImages = new ArrayList<>(imageCount.get(0));
			for(int i = 0; i < pSwapchainImages.capacity(); i++) {
				swapchainImages.add(pSwapchainImages.get(i));
			}
			swapchainImageFormat = formats.format();
			swapchainExtent = VkExtent2D.create().set(extent);
			
			swapchainImageViews = new ArrayList<>(swapchainImages.size());
			for(long swapchainImage : swapchainImages) {
				swapchainImageViews.add(createImageView(swapchainImage, VK_FORMAT_B8G8R8A8_UNORM));
			}
		}
	}
	
	private static void createShaderStage() {
		try(var stack = stackPush()){
			ByteBuffer vsh = null;
			ByteBuffer fsh = null;
			shEPName = memUTF8("main");
			
			var cde0 = LaunchRenderer3.class.getResourceAsStream(VSH_NAME);
			if(cde0 == null) {
				throw new RuntimeException("Vertex shader has not found\n");
			}
			try {
				byte[] bf = cde0.readAllBytes();
				vsh = stack.malloc(bf.length);
				vsh.put(0, bf);
				cde0.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			var cde1 = LaunchRenderer3.class.getResourceAsStream(FSH_NAME);
			if(cde1 == null) {
				throw new RuntimeException("Fragment shader has not found\n");
			}
			try {
				byte[] bf = cde1.readAllBytes();
				fsh = stack.malloc(bf.length);
				fsh.put(0, bf);
				cde1.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(vsh == null || fsh == null) {
				throw new RuntimeException("Shader has not found\n");
			}
			var pVsh = stack.mallocLong(1);
			var shCreateInfo = VkShaderModuleCreateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
			shCreateInfo.pCode(vsh);
			if(vkCreateShaderModule(device, shCreateInfo, null, pVsh) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create vertex shader module\n");
			}
			var pFsh = stack.mallocLong(1);
			shCreateInfo.pCode(fsh);
			if(vkCreateShaderModule(device, shCreateInfo, null, pFsh) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create fragment shader module\n");
			}
			prtUI = VkPipelineShaderStageCreateInfo.calloc(2);
			prtUI.get(0)
					.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
					.stage(VK_SHADER_STAGE_VERTEX_BIT)
					.module(pVsh.get(0))
					.pName(shEPName);
			prtUI.get(1)
					.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
					.stage(VK_SHADER_STAGE_FRAGMENT_BIT)
					.module(pFsh.get(0))
					.pName(shEPName);
			
			
		}
	}
	
	private static void createGraphicsPipeline() {
		try(var stack = stackPush()){
			var pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
					.pSetLayouts(stack.longs(descriptorSetLayout));
			var pPipelineLayout = stack.longs(VK_NULL_HANDLE);
			if(vkCreatePipelineLayout(device, pipelineLayoutInfo, null, pPipelineLayout) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create pipeline layout\n");
			}
			pipelineLayout = pPipelineLayout.get(0);
			
			var renderPassInfo = VkRenderPassCreateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
					.pAttachments(VkAttachmentDescription.calloc(1, stack)
							.format(swapchainImageFormat)
							.samples(VK_SAMPLE_COUNT_1_BIT)
							.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
							.storeOp(VK_ATTACHMENT_STORE_OP_STORE)
							.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
							.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
							.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
							.finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR))
					.pSubpasses(VkSubpassDescription.calloc(1, stack)
							.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
							.colorAttachmentCount(1)
							.pColorAttachments(VkAttachmentReference.calloc(1, stack)
									.attachment(0)
									.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)))
					.pDependencies(VkSubpassDependency.calloc(1, stack)
							.srcSubpass(VK_SUBPASS_EXTERNAL)
							.dstSubpass(0)
							.srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
							.srcAccessMask(0)
							.dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
							.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT
									| VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT));
			var pRenderPass = stack.mallocLong(1);
			if(vkCreateRenderPass(device, renderPassInfo, null, pRenderPass) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create render pass\n");
			}
			renderPass = pRenderPass.get(0);
			
			var pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack)
					.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
					.pStages(prtUI)
					.pVertexInputState(VkPipelineVertexInputStateCreateInfo.calloc(stack)
							.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
							.pVertexBindingDescriptions(Vertex.getBindingDescription(stack))
							.pVertexAttributeDescriptions(Vertex.getAttributeDescriptions(stack)))
					.pInputAssemblyState(VkPipelineInputAssemblyStateCreateInfo.calloc(stack)
							.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
							.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
							.primitiveRestartEnable(false))
					.pViewportState(VkPipelineViewportStateCreateInfo.calloc(stack)
							.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
							.pViewports(VkViewport.calloc(1, stack)
									.x(0.0f)
									.y(0.0f)
									.width(cw)
									.height(ch)
									.minDepth(0.0f)
									.maxDepth(1.0f))
							.pScissors(VkRect2D.calloc(1, stack)
									.offset(VkOffset2D.calloc(stack).set(0, 0))
									.extent(swapchainExtent)))
					.pRasterizationState(VkPipelineRasterizationStateCreateInfo.calloc(stack)
							.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
							.depthClampEnable(false)
							.rasterizerDiscardEnable(false)
							.polygonMode(VK_POLYGON_MODE_FILL)
							.lineWidth(1.0f)
							.cullMode(VK_CULL_MODE_BACK_BIT)
							.frontFace(VK_FRONT_FACE_CLOCKWISE)
							.depthBiasEnable(false))
					.pMultisampleState(VkPipelineMultisampleStateCreateInfo.calloc(stack)
							.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
							.sampleShadingEnable(false)
							.rasterizationSamples(VK_SAMPLE_COUNT_1_BIT))
					.pColorBlendState(VkPipelineColorBlendStateCreateInfo.calloc(stack)
							.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
							.logicOpEnable(false)
							.logicOp(VK_LOGIC_OP_COPY)
							.pAttachments(VkPipelineColorBlendAttachmentState.calloc(1, stack)
									.colorWriteMask(VK_COLOR_COMPONENT_R_BIT
											| VK_COLOR_COMPONENT_G_BIT
											| VK_COLOR_COMPONENT_B_BIT
											| VK_COLOR_COMPONENT_A_BIT)
									.blendEnable(false))
							.blendConstants(stack.floats(0.0f, 0.0f, 0.0f, 0.0f)))
					.layout(pipelineLayout)
					.renderPass(renderPass)
					.subpass(0)
					.basePipelineHandle(VK_NULL_HANDLE)
					.basePipelineIndex(-1);
				var pGraphicsPipeline = stack.mallocLong(1);
				if(vkCreateGraphicsPipelines(
						device,
						VK_NULL_HANDLE,
						pipelineInfo,
						null,
						pGraphicsPipeline) != VK_SUCCESS) {
					throw new RuntimeException("Failed to create graphics pipeline\n");
				}
				graphicsPipeline = pGraphicsPipeline.get(0);
		}
	}
	
	private static void createFramebuffers() {
		swapchainFramebuffers = new ArrayList<>(swapchainImageViews.size());
		try(var stack = stackPush()){
			var attachments = stack.mallocLong(1);
			var pFramebuffer = stack.mallocLong(1);
			var framebufferInfo = VkFramebufferCreateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
					.renderPass(renderPass)
					.width(swapchainExtent.width())
					.height(swapchainExtent.height())
					.layers(1);
			for(long imageView : swapchainImageViews) {
				attachments.put(0, imageView);
				framebufferInfo.pAttachments(attachments);
				if(vkCreateFramebuffer(device, framebufferInfo, null, pFramebuffer) != VK_SUCCESS) {
					throw new RuntimeException("Failed to create framebuffer\n");
				}
				swapchainFramebuffers.add(pFramebuffer.get(0));
			}
		}
	}
	
	private static void createSyncObjects() {
		inFlightFrames = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);
		imagesInFlight = new HashMap<>(swapchainImages.size());
		
		try(var stack = stackPush()){
			var semaphoreInfo = VkSemaphoreCreateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
			var fenceInfo = VkFenceCreateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)
					.flags(VK_FENCE_CREATE_SIGNALED_BIT);
			var pImageAvailableSemaphore = stack.mallocLong(1);
			var pRenderFinishedSemaphore = stack.mallocLong(1);
			var pFence = stack.mallocLong(1);
			var allocInfo = VkCommandBufferAllocateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
					.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
					.commandPool(computeCommandPool)
					.commandBufferCount(1);
			var pCommandBuffer = stack.mallocPointer(1);
			for(int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
				if(vkCreateSemaphore(device, semaphoreInfo, null, pImageAvailableSemaphore) != VK_SUCCESS
						|| vkCreateSemaphore(device, semaphoreInfo, null, pRenderFinishedSemaphore) != VK_SUCCESS
						|| vkCreateFence(device, fenceInfo, null, pFence) != VK_SUCCESS) {
					throw new RuntimeException("Failed to create synchronization objects for the frame\n");
				}
				vkAllocateCommandBuffers(device, allocInfo, pCommandBuffer);
				var frame = new Frame(
						pImageAvailableSemaphore.get(0), pRenderFinishedSemaphore.get(0), pFence.get(0));
				frame.cb = new VkCommandBuffer(pCommandBuffer.get(0), device);
				inFlightFrames.add(frame);
			}
		}
	}
	
	private static void createDescriptorSetLayout() {
		try(var stack = stackPush()){
			var bindings = VkDescriptorSetLayoutBinding.calloc(1, stack);
			bindings.get(0)
					.binding(0)
					.descriptorCount(1)
					.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
					.pImmutableSamplers(null)
					.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);
			
			var layoutInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
					.pBindings(bindings);
			
			var pDescriptorSetLayout = stack.mallocLong(1);
			if(vkCreateDescriptorSetLayout(device, layoutInfo, null, pDescriptorSetLayout) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create descriptor set layout\n");
			}
			descriptorSetLayout = pDescriptorSetLayout.get(0);
		}
	}
	
	private static void createDescriptorPool() {
		try(var stack = stackPush()){
			var poolSizes = VkDescriptorPoolSize.calloc(1, stack);
			poolSizes.get(0).type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
					.descriptorCount(swapchainImages.size());
			
			var poolInfo = VkDescriptorPoolCreateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
					.pPoolSizes(poolSizes)
					.maxSets(swapchainImages.size());
			
			var pDescriptorPool = stack.mallocLong(1);
			if(vkCreateDescriptorPool(device, poolInfo, null, pDescriptorPool)  != VK_SUCCESS) {
				throw new RuntimeException("Failed to create descriptor pool\n");
			}
			descriptorPool = pDescriptorPool.get(0);
		}
	}
	
	private static void createDescriptorSets() {
		try(var stack = stackPush()){
			var layouts = stack.mallocLong(swapchainImages.size());
			for(int i = 0; i < layouts.capacity(); i++) {
				layouts.put(i, descriptorSetLayout);
			}
			var allocInfo = VkDescriptorSetAllocateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
					.descriptorPool(descriptorPool)
					.pSetLayouts(layouts);
			
			var pDescriptorSets = stack.mallocLong(swapchainImages.size());
			if(vkAllocateDescriptorSets(device, allocInfo, pDescriptorSets) != VK_SUCCESS) {
				throw new RuntimeException("Failed to allocate descriptor sets\n");
			}
			
			descriptorSets = new ArrayList<>(pDescriptorSets.capacity());
			
			var imageInfo = VkDescriptorImageInfo.calloc(1, stack);
			imageInfo.imageLayout(VK_IMAGE_LAYOUT_GENERAL);
			
			var descriptorWrites = VkWriteDescriptorSet.calloc(1, stack);
			descriptorWrites.get(0)
					.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
					.dstBinding(0)
					.dstArrayElement(0)
					.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
					.descriptorCount(1);
			
			for(int i = 0; i < pDescriptorSets.capacity(); i++) {
				long descriptorSet = pDescriptorSets.get(i);
				
				imageInfo.imageView(gBufferImages.get(i).view)
						.sampler(gBufferImages.get(i).sampler);
				descriptorWrites.get(0)
						.pImageInfo(imageInfo);
				
				descriptorWrites.get(0).dstSet(descriptorSet);
				
				vkUpdateDescriptorSets(device, descriptorWrites, null);
				descriptorSets.add(descriptorSet);
			}
		}
	}
	
	private static void createTextureImage() {
		try(var stack = stackPush()){
//			String fileName = Paths.get(new URI(getSystemClassLoader()
//					.getResource(TEXTURE_NAME).toExternalForm())).toString();
			String fileName = "assets/shaft_tex_3.png";
			var pWidth = stack.mallocInt(1);
			var pHeight = stack.mallocInt(1);
			var pChannels = stack.mallocInt(1);
			
			ByteBuffer pixels = stbi_load(fileName, pWidth, pHeight, pChannels, STBI_rgb_alpha);
			long imageSize = pWidth.get(0) * pHeight.get(0) * pChannels.get(0);
			if(pixels == null) {
				throw new RuntimeException("Failed to load texture image\n");
			}
			
			var pSBuffer = stack.mallocLong(1);
			var pSBufferMemory = stack.mallocLong(1);
			createBuffer(imageSize,
					VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
					VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
					pSBuffer,
					pSBufferMemory);
			
			var data = stack.mallocPointer(1);
			vkMapMemory(device, pSBufferMemory.get(0), 0, imageSize, 0, data);
			memcpy(data.getByteBuffer(0, (int)imageSize), pixels, imageSize);
			vkUnmapMemory(device, pSBufferMemory.get(0));
			
			stbi_image_free(pixels);
			
			textureImage = new Image();
			textureImage.width = pWidth.get(0);
			textureImage.height = pHeight.get(0);
			textureImage.format = VK_FORMAT_R8G8B8A8_UINT;
			textureImage.tiling = VK_IMAGE_TILING_LINEAR; // TILING_OPTIMAL
			textureImage.usage = VK_IMAGE_USAGE_TRANSFER_DST_BIT
					| VK_IMAGE_USAGE_SAMPLED_BIT
					| VK_IMAGE_USAGE_TRANSFER_SRC_BIT
					| VK_IMAGE_USAGE_STORAGE_BIT;
			createImage(textureImage, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
			
			transitionImageLayout(textureImage,
					VK_IMAGE_LAYOUT_UNDEFINED,
					VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
			
			copyBufferToImage(pSBuffer.get(0), textureImage);
			
			transitionImageLayout(textureImage,
					VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
					VK_IMAGE_LAYOUT_GENERAL);
			
			vkDestroyBuffer(device, pSBuffer.get(0), null);
			vkFreeMemory(device, pSBufferMemory.get(0), null);
			
			textureImage.view = createImageView(textureImage.image, textureImage.format);
			
//			fileName = Paths.get(new URI(getSystemClassLoader()
//					.getResource(BACKGROUND_NAME).toExternalForm())).toString();
			fileName = "assets/backgroundx.png";
			pixels = null;
			pixels = stbi_load(fileName, pWidth, pHeight, pChannels, STBI_rgb_alpha);
			imageSize = pWidth.get(0) * pHeight.get(0) * pChannels.get(0);
			if(pixels == null) {
				throw new RuntimeException("Failed to load texture image\n");
			}
			
			createBuffer(imageSize,
					VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
					VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
					pSBuffer,
					pSBufferMemory);
			vkMapMemory(device, pSBufferMemory.get(0), 0, imageSize, 0, data);
			memcpy(data.getByteBuffer(0, (int)imageSize), pixels, imageSize);
			vkUnmapMemory(device, pSBufferMemory.get(0));
			
			stbi_image_free(pixels);
			
			backgroundImage = new Image();
			backgroundImage.width = pWidth.get(0);
			backgroundImage.height = pHeight.get(0);
			backgroundImage.format = VK_FORMAT_R8G8B8A8_UINT;
			backgroundImage.tiling = VK_IMAGE_TILING_LINEAR;
			backgroundImage.usage = VK_IMAGE_USAGE_TRANSFER_DST_BIT
					| VK_IMAGE_USAGE_SAMPLED_BIT
					| VK_IMAGE_USAGE_STORAGE_BIT;
			createImage(backgroundImage, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
			
			transitionImageLayout(backgroundImage,
					VK_IMAGE_LAYOUT_UNDEFINED,
					VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
			
			copyBufferToImage(pSBuffer.get(0), backgroundImage);
			
			transitionImageLayout(backgroundImage,
					VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
					VK_IMAGE_LAYOUT_GENERAL);
			
			vkDestroyBuffer(device, pSBuffer.get(0), null);
			vkFreeMemory(device, pSBufferMemory.get(0), null);
			
			backgroundImage.view = createImageView(backgroundImage.image, backgroundImage.format);
			
		}
//		catch(URISyntaxException e) {
//			e.printStackTrace();
//		}
	}
	
	private static long createImageView(long image, int format) {
		try(var stack = stackPush()){
			var viewInfo = VkImageViewCreateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
					.image(image)
					.viewType(VK_IMAGE_VIEW_TYPE_2D)
					.format(format)
					.subresourceRange(VkImageSubresourceRange.calloc(stack)
							.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
							.baseMipLevel(0)
							.levelCount(1)
							.baseArrayLayer(0)
							.layerCount(1));
			var pImageView = stack.mallocLong(1);
			if(vkCreateImageView(device, viewInfo, null, pImageView) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create texture image view\n");
			}
			
			return pImageView.get(0);
		}
	}
	
	private static long createTextureSampler() {
		try(var stack = stackPush()){
			var samplerInfo = VkSamplerCreateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO)
					.magFilter(VK_FILTER_NEAREST)
					.minFilter(VK_FILTER_NEAREST)
					.addressModeU(VK_SAMPLER_ADDRESS_MODE_REPEAT)
					.addressModeV(VK_SAMPLER_ADDRESS_MODE_REPEAT)
					.addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT)
					.anisotropyEnable(false)
					.borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK)
					.unnormalizedCoordinates(false)
					.compareEnable(false)
					.compareOp(VK_COMPARE_OP_ALWAYS)
					.mipmapMode(VK_SAMPLER_MIPMAP_MODE_NEAREST);
			var pTextureSampler = stack.mallocLong(1);
			if(vkCreateSampler(device, samplerInfo, null, pTextureSampler) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create texture sampler\n");
			}
			
			return pTextureSampler.get(0);
		}
	}
	
	private static void createGBufferImages() {
		int size = swapchainImages.size();
		try(var stack = stackPush()){
			gBufferImages = new ArrayList<>(swapchainImages.size());
			int imageSize = 1024 * 1024 * Integer.BYTES;
			
			var testData = memCalloc(imageSize);
			var pSBuffer = stack.mallocLong(1);
			var pSBufferMemory = stack.mallocLong(1);
			var data = stack.mallocPointer(1);
			
			for(int i = 0; i < size; i++) {
				createBuffer(imageSize,
						VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
						VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
						pSBuffer,
						pSBufferMemory);
				
				vkMapMemory(device, pSBufferMemory.get(0), 0, imageSize, 0, data);
				memcpy(data.getByteBuffer(0, (int)imageSize), testData, imageSize);
				vkUnmapMemory(device, pSBufferMemory.get(0));
				
				Image img = new Image();
				img.width = 1024;
				img.height = 1024;
				img.tiling = VK_IMAGE_TILING_LINEAR;
				img.format = VK_FORMAT_R8G8B8A8_UINT;
				img.usage = VK_IMAGE_USAGE_SAMPLED_BIT
						| VK_IMAGE_USAGE_STORAGE_BIT
						| VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT
						| VK_IMAGE_USAGE_TRANSFER_SRC_BIT
						| VK_IMAGE_USAGE_TRANSFER_DST_BIT;
			
				createImage(img, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
			
				transitionImageLayout(img, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
				
				copyBufferToImage(pSBuffer.get(0), img);
				
				transitionImageLayout(img,
						VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
						VK_IMAGE_LAYOUT_GENERAL);
				
				vkDestroyBuffer(device, pSBuffer.get(0), null);
				vkFreeMemory(device, pSBufferMemory.get(0), null);
			
				img.view = createImageView(img.image, img.format);
				img.sampler = createTextureSampler();
				
				gBufferImages.add(img);
			}
			var gList = gBufferImages.stream().map(i -> i.view).toList();
			cpyShader = new SingleCopy(textureImage.view, gList, size).createPipeline();
			bgShader = new SingleCopy(backgroundImage.view, gList, size).createPipeline();
			strShader = new ASCII8x20Copy(textureImage.view, gList, size).createPipeline();
			colorShader = new ColorCopy(gList, size).createPipeline();
			
			memFree(testData);
		}
	}
	
	private static void createFixedPositionsBuffers() {
		try(var stack = stackPush()){
			var fd = CellCopy.createFixedPositions(stack);
			var pVBuffer = stack.mallocLong(1);
			var pVBufferMemory = stack.mallocLong(1);
			int size = 520 * 2 * Integer.BYTES;
			createBuffer(size,
					VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
					VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
					pVBuffer,
					pVBufferMemory);
			
			var data = stack.mallocPointer(1);
			vkMapMemory(device, pVBufferMemory.get(0), 0, size, 0, data);
			memcpy(data.getIntBuffer(0, 520 * 2), fd, 520 * 2);
			vkUnmapMemory(device, pVBufferMemory.get(0));
			
			long sVBuffer = pVBuffer.get(0);
			long sVBufferMemory = pVBufferMemory.get(0);
			
			createBuffer(size,
					VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
					VK_MEMORY_HEAP_DEVICE_LOCAL_BIT,
					pVBuffer,
					pVBufferMemory);
			
			cellPositionsBuffer = new Buffer();
			cellPositionsBuffer.offset = 0;
			cellPositionsBuffer.length = size;
			cellPositionsBuffer.buffer = pVBuffer.get(0);
			cellPositionsBuffer.memory = pVBufferMemory.get(0);
			
			copyBuffer(sVBuffer, cellPositionsBuffer.buffer, size);
			
			vkDestroyBuffer(device, sVBuffer, null);
			vkFreeMemory(device, sVBufferMemory, null);
			
			fd = ControlCopy.createFixedPositions(stack);
			size = 12 * 2 * Integer.BYTES;
			createBuffer(size,
					VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
					VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
					pVBuffer,
					pVBufferMemory);
			
			vkMapMemory(device, pVBufferMemory.get(0), 0, size, 0, data);
			memcpy(data.getIntBuffer(0, 12 * 2), fd, 12 * 2);
			vkUnmapMemory(device, pVBufferMemory.get(0));
			
			sVBuffer = pVBuffer.get(0);
			sVBufferMemory = pVBufferMemory.get(0);
			
			createBuffer(size,
					VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
					VK_MEMORY_HEAP_DEVICE_LOCAL_BIT,
					pVBuffer,
					pVBufferMemory);
			
			controlPositionsBuffer = new Buffer();
			controlPositionsBuffer.offset = 0;
			controlPositionsBuffer.length = size;
			controlPositionsBuffer.buffer = pVBuffer.get(0);
			controlPositionsBuffer.memory = pVBufferMemory.get(0);
			
			copyBuffer(sVBuffer, controlPositionsBuffer.buffer, size);
			
			vkDestroyBuffer(device, sVBuffer, null);
			vkFreeMemory(device, sVBufferMemory, null);
			
			fd = GuideCopy.createFixedPositions(stack);
			size = 12 * 2 * Integer.BYTES;
			createBuffer(size,
					VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
					VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
					pVBuffer,
					pVBufferMemory);
			
			vkMapMemory(device, pVBufferMemory.get(0), 0, size, 0, data);
			memcpy(data.getIntBuffer(0, 12 * 2), fd, 12 * 2);
			vkUnmapMemory(device, pVBufferMemory.get(0));
			
			sVBuffer = pVBuffer.get(0);
			sVBufferMemory = pVBufferMemory.get(0);
			
			createBuffer(size,
					VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
					VK_MEMORY_HEAP_DEVICE_LOCAL_BIT,
					pVBuffer,
					pVBufferMemory);
			
			guidePositionsBuffer = new Buffer();
			guidePositionsBuffer.offset = 0;
			guidePositionsBuffer.length = size;
			guidePositionsBuffer.buffer = pVBuffer.get(0);
			guidePositionsBuffer.memory = pVBufferMemory.get(0);
			
			copyBuffer(sVBuffer, guidePositionsBuffer.buffer, size);
			
			vkDestroyBuffer(device, sVBuffer, null);
			vkFreeMemory(device, sVBufferMemory, null);
			
			fd = SegmentCopy.createFixedPositions(stack);
			size = 48 * 2 * Integer.BYTES;
			createBuffer(size,
					VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
					VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
					pVBuffer,
					pVBufferMemory);
			
			vkMapMemory(device, pVBufferMemory.get(0), 0, size, 0, data);
			memcpy(data.getIntBuffer(0, 48 * 2), fd, 48 * 2);
			vkUnmapMemory(device, pVBufferMemory.get(0));
			
			sVBuffer = pVBuffer.get(0);
			sVBufferMemory = pVBufferMemory.get(0);
			
			createBuffer(size,
					VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
					VK_MEMORY_HEAP_DEVICE_LOCAL_BIT,
					pVBuffer,
					pVBufferMemory);
			
			segmentPositionsBuffer = new Buffer();
			segmentPositionsBuffer.offset = 0;
			segmentPositionsBuffer.length = size;
			segmentPositionsBuffer.buffer = pVBuffer.get(0);
			segmentPositionsBuffer.memory = pVBufferMemory.get(0);
			
			copyBuffer(sVBuffer, segmentPositionsBuffer.buffer, size);
			
			vkDestroyBuffer(device, sVBuffer, null);
			vkFreeMemory(device, sVBufferMemory, null);
			
			size = 800 * 900 * 4;
			createBuffer(size,
					VK_BUFFER_USAGE_TRANSFER_DST_BIT,
					VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
					pVBuffer,
					pVBufferMemory);
			
			screenShotBuffer = new Buffer();
			screenShotBuffer.offset = 0;
			screenShotBuffer.length = size;
			screenShotBuffer.buffer = pVBuffer.get(0);
			screenShotBuffer.memory = pVBufferMemory.get(0);
		}
	}
	
	private static void createUniformBuffers() {
		try(var stack = stackPush()){
			var tp = stack.mallocPointer(1);
			
			var pBuffer = stack.mallocLong(1);
			var pBufferMemory = stack.mallocLong(1);
			
			int size = 520 * Integer.BYTES;
			cellStatesBuffers = new ArrayList<>(swapchainImages.size());
			
			var td = stack.callocInt(520);
			td.put(350, 0xCFCFCF02);
			td.put(351, 0x00000001);
			td.put(352, 0x0008FF04);
			td.put(365, 0x00000001);
			td.put(379, 0xFFCC0002);
			
			for(int i = 0; i < swapchainImages.size(); i++) {
				createBuffer(
						size,
						VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
						VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
						pBuffer,
						pBufferMemory);
				var b = new Buffer();
				b.offset = 0;
				b.length = size;
				b.buffer = pBuffer.get(0);
				b.memory = pBufferMemory.get(0);
				cellStatesBuffers.add(b);
				
				// TEST DATA
				vkMapMemory(device, b.memory, b.offset, b.length, 0, tp);
				memcpy(tp.getIntBuffer(0, 520), td, 520);
				vkUnmapMemory(device, b.memory);
			}
			
			size = 12 * 2 * Integer.BYTES;
			controlStatesBuffers = new ArrayList<>(swapchainImages.size());
			
			for(int i = 0; i < swapchainImages.size(); i++) {
				createBuffer(
						size,
						VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
						VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
						pBuffer,
						pBufferMemory
						);
				var b = new Buffer();
				b.offset = 0;
				b.length = size;
				b.buffer = pBuffer.get(0);
				b.memory = pBufferMemory.get(0);
				controlStatesBuffers.add(b);
			}
			
			size = 12 * Integer.BYTES;
			guideStatesBuffers = new ArrayList<>(swapchainImages.size());
			
			for(int i = 0; i < swapchainImages.size(); i++) {
				createBuffer(
						size,
						VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
						VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
						pBuffer,
						pBufferMemory
						);
				var b = new Buffer();
				b.offset = 0;
				b.length = size;
				b.buffer = pBuffer.get(0);
				b.memory = pBufferMemory.get(0);
				guideStatesBuffers.add(b);
			}
			
			size = 48 * Integer.BYTES;
			segmentStatesBuffers = new ArrayList<>(swapchainImages.size());
			
			for(int i = 0; i < swapchainImages.size(); i++) {
				createBuffer(
						size,
						VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
						VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
						pBuffer,
						pBufferMemory
						);
				var b = new Buffer();
				b.offset = 0;
				b.length = size;
				b.buffer = pBuffer.get(0);
				b.memory = pBufferMemory.get(0);
				segmentStatesBuffers.add(b);
			}
			
			var gList = gBufferImages.stream().map(i -> i.view).toList();
			cellShader = new CellCopy(textureImage.view,
					gList,
					cellPositionsBuffer.buffer,
					cellStatesBuffers.stream().map(i -> i.buffer).toList(),
					swapchainImages.size())
					.createPipeline();
			ctrlShader = new ControlCopy(textureImage.view,
					gList,
					controlPositionsBuffer.buffer,
					controlStatesBuffers.stream().map(i -> i.buffer).toList(),
					swapchainImages.size())
					.createPipeline();
			guideShader = new GuideCopy(textureImage.view,
					gList,
					guidePositionsBuffer.buffer,
					guideStatesBuffers.stream().map(i -> i.buffer).toList(),
					swapchainImages.size())
					.createPipeline();
			segmentShader = new SegmentCopy(textureImage.view,
					gList,
					segmentPositionsBuffer.buffer,
					segmentStatesBuffers.stream().map(i -> i.buffer).toList(),
					swapchainImages.size())
					.createPipeline();
		}
	}
	
	static void updateCells(int imageIdx, int idx, int length, int[] src) {
		int didx = idx - 88;
		var b = cellStatesBuffers.get(imageIdx);
		try(var stack = stackPush()){
			var tp = stack.mallocPointer(1);
			vkMapMemory(device, b.memory, b.offset, b.length, 0, tp);
			var d = tp.getIntBuffer(520);
			for(int i = idx; i < length + idx; i++) {
				d.put(didx, src[i]);
				didx++;
			}
			vkUnmapMemory(device, b.memory);
		}
	}
	
	static void updateSegment(int imageIdx, int idx, int length, int[] src) {
		var b = segmentStatesBuffers.get(imageIdx);
		try(var stack = stackPush()){
			var tp = stack.mallocPointer(1);
			vkMapMemory(device, b.memory, b.offset, b.length, 0, tp);
			var d = tp.getIntBuffer(48);
			for(int i = idx; i < length + idx; i++) {
				d.put(i, src[i]);
			}
			vkUnmapMemory(device, b.memory);
		}
	}
	
	static void updateGuide(int imageIdx, int[] src) {
		var b = guideStatesBuffers.get(imageIdx);
		try(var stack = stackPush()){
			var tp = stack.mallocPointer(1);
			vkMapMemory(device, b.memory, b.offset, b.length, 0, tp);
			var d = tp.getIntBuffer(12);
			for(int i = 0; i < 12; i++) {
				d.put(i, src[i]);
			}
			vkUnmapMemory(device, b.memory);
		}
	}
	
	static void updateControl(int imageIdx, int[] src) {
		var b = controlStatesBuffers.get(imageIdx);
		try(var stack = stackPush()){
			var tp = stack.mallocPointer(1);
			vkMapMemory(device, b.memory, b.offset, b.length, 0, tp);
			var d = tp.getIntBuffer(24);
			for(int i = 0; i < 24; i++) {
				d.put(i, src[i]);
			}
			vkUnmapMemory(device, b.memory);
		}
	}
	
	static void requestResize(int w, int h) {
		cw = w;
		ch = h;
		requestedResize = true;
	}
	
	private static void cleanupSwapchain() {
		cellStatesBuffers.forEach(b -> Buffer.free(device, b));
		controlStatesBuffers.forEach(b -> Buffer.free(device, b));
		guideStatesBuffers.forEach(b -> Buffer.free(device, b));
		segmentStatesBuffers.forEach(b -> Buffer.free(device, b));
		
		vkDestroyDescriptorPool(device, descriptorPool, null);
		
		swapchainFramebuffers.forEach(fb -> vkDestroyFramebuffer(device, fb, null));
		gBufferImages.forEach(i -> Image.free(device, i));
		vkDestroyPipeline(device, graphicsPipeline, null);
		vkDestroyPipelineLayout(device, pipelineLayout, null);
		vkDestroyRenderPass(device, renderPass, null);
		cpyShader.freePipeline();
		bgShader.freePipeline();
		strShader.freePipeline();
		cellShader.freePipeline();
		ctrlShader.freePipeline();
		guideShader.freePipeline();
		segmentShader.freePipeline();
		colorShader.freePipeline();
		swapchainImageViews.forEach(iv -> vkDestroyImageView(device, iv, null));
		vkDestroySwapchainKHR(device, swapchain, null);
	}
	
	private static void recreateSwapchain(int width, int height) {
		vkDeviceWaitIdle(device);
		cleanupSwapchain();
		createSwapchainObjects(width, height);
	}
	
	private static void drawFrame() {
		try(var stack = stackPush()){
			Frame thisFrame = inFlightFrames.get(currentFrame);
			vkWaitForFences(device, stack.longs(thisFrame.fence), true, UINT64_MAX);
			if(screenShotDrew != -1) {
				singleTimeCommand(stack, (c, s) -> {
					var region = VkBufferImageCopy.calloc(1, stack)
							.bufferOffset(0)
							.bufferRowLength(0)
							.bufferImageHeight(0)
							.imageSubresource(VkImageSubresourceLayers.calloc(stack)
									.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
									.mipLevel(0)
									.baseArrayLayer(0)
									.layerCount(1))
							.imageOffset(VkOffset3D.calloc(stack)
									.x(0)
									.y(0)
									.z(0))
							.imageExtent(VkExtent3D.calloc(stack)
									.width(800)
									.height(900)
									.depth(1));
					vkCmdCopyImageToBuffer(c,
							gBufferImages.get(screenShotDrew).image,
							VK_IMAGE_LAYOUT_GENERAL,
							screenShotBuffer.buffer,
							region);
				});
				var data = stack.mallocPointer(1);
				var dstBuffer = memAlloc(screenShotBuffer.length);
				vkMapMemory(device,
						screenShotBuffer.memory,
						screenShotBuffer.offset,
						screenShotBuffer.length, 0, data);
				memcpy(dstBuffer, data.getByteBuffer(0, screenShotBuffer.length), screenShotBuffer.length);
				vkUnmapMemory(device, screenShotBuffer.memory);
				Calendar cd = Calendar.getInstance();
				String fn = String.format("p%d%02d%02d_%02d%02d%02d.png",
						cd.get(Calendar.YEAR),
						cd.get(Calendar.MONTH) + 1,
						cd.get(Calendar.DAY_OF_MONTH),
						cd.get(Calendar.HOUR_OF_DAY),
						cd.get(Calendar.MINUTE),
						cd.get(Calendar.SECOND));
				/* boolean r = */stbi_write_png(fn, 800, 900, 4, dstBuffer, 800 * 4);
				// System.out.println(r);
				memFree(dstBuffer);
				screenShotDrew = -1;
			}
			var pImageIdx = stack.mallocInt(1);
			int result = vkAcquireNextImageKHR(
					device, swapchain, UINT64_MAX, thisFrame.imageAvailableSemaphore, VK_NULL_HANDLE, pImageIdx);
			if(result == VK_ERROR_OUT_OF_DATE_KHR) {
				recreateSwapchain(cw, ch);
				return;
			} if(result != VK_SUCCESS && result != VK_SUBOPTIMAL_KHR) {
				throw new RuntimeException("Cannot get image ("+ result +")\n");
			}
			int imageIdx = pImageIdx.get(0);
			if(imagesInFlight.containsKey(imageIdx)) {
				vkWaitForFences(device, imagesInFlight.get(imageIdx).fence, true, UINT64_MAX);
			}
			imagesInFlight.put(imageIdx, thisFrame);
			
			Launch.mainLoop();
			Launch.copyToUniformBuffer(imageIdx);
			
			// *******
			beginSingleTimeCommand2(stack, thisFrame);
			if(result != VK_SUBOPTIMAL_KHR) {
				var c = thisFrame.cb;
				
				Launch.draw(c, imageIdx);
				
				if(requestedScreenShot) {
					requestedScreenShot = false;
					screenShotDrew = imageIdx;
				}
									
				var clv = VkClearValue.calloc(1, stack);
				clv.color().float32(stack.floats(0.0f, 0.0f, 1.0f, 1.0f));
				var renderPassInfo = VkRenderPassBeginInfo.calloc(stack)
						.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
						.renderPass(renderPass)
						.renderArea(VkRect2D.calloc(stack)
								.offset(VkOffset2D.calloc(stack).set(0, 0))
								.extent(swapchainExtent))
						.pClearValues(clv);
					
				renderPassInfo.framebuffer(swapchainFramebuffers.get(imageIdx));
					
				vkCmdBeginRenderPass(c, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
				vkCmdBindPipeline(c, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);
				vkCmdBindVertexBuffers(c, 0, stack.longs(vertexBuffer), stack.longs(0));
				vkCmdBindIndexBuffer(c, indexBuffer, 0, VK_INDEX_TYPE_UINT16);
				vkCmdBindDescriptorSets(c,
						VK_PIPELINE_BIND_POINT_GRAPHICS,
						pipelineLayout, 0, stack.longs(descriptorSets.get(imageIdx)), null);
				vkCmdDrawIndexed(c, Vertex.INDICES.length, 1, 0, 0, 0);
				vkCmdEndRenderPass(c);
			}
			endSingleTimeCommand2(stack, thisFrame);
			
			Launch.afterDraw();
			
			var presentInfo = VkPresentInfoKHR.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
					.pWaitSemaphores(stack.longs(thisFrame.renderFinishedSemaphore))
					.swapchainCount(1)
					.pSwapchains(stack.longs(swapchain))
					.pImageIndices(pImageIdx);
			result = vkQueuePresentKHR(computeQueue, presentInfo);
			
			if(result == VK_ERROR_OUT_OF_DATE_KHR || requestedResize || requestedVSync) {
				requestedResize = false;
				requestedVSync = false;
				glfwSetWindowSize(window, cw, ch);
				recreateSwapchain(cw, ch);
			} else if(result == VK_SUBOPTIMAL_KHR) {
				var w = stack.callocInt(1);
				var h = stack.callocInt(1);
				glfwGetFramebufferSize(window, w, h);
				if(w.get(0) != 0 && h.get(0) != 0) {
					recreateSwapchain(cw, ch);
				}
				
			} else if(result != VK_SUCCESS) {
				throw new RuntimeException("Failed to present swap chain image\n");
			}
			
			currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;
		}
	}
	
	private static void cleanup() {
		cleanupSwapchain();
		
		Launch.free();
		
		SingleCopy.freeAll();
		ASCII8x20Copy.freeAll();
		CellCopy.freeAll();
		ControlCopy.freeAll();
		GuideCopy.freeAll();
		SegmentCopy.freeAll();
		ColorCopy.freeAll();
		Image.free(device, textureImage);
		Image.free(device, backgroundImage);
		Buffer.free(device, cellPositionsBuffer);
		Buffer.free(device, controlPositionsBuffer);
		Buffer.free(device, guidePositionsBuffer);
		Buffer.free(device, segmentPositionsBuffer);
		Buffer.free(device, screenShotBuffer);
		
		vkDestroyDescriptorSetLayout(device, descriptorSetLayout, null);
		
		vkDestroyBuffer(device, indexBuffer, null);
		vkFreeMemory(device, indexBufferMemory, null);
		vkDestroyBuffer(device, vertexBuffer, null);
		vkFreeMemory(device, vertexBufferMemory, null);
		
		inFlightFrames.forEach(f -> {
			vkDestroySemaphore(device, f.renderFinishedSemaphore, null);
			vkDestroySemaphore(device, f.imageAvailableSemaphore, null);
			vkDestroyFence(device, f.fence, null);
		});
		inFlightFrames.clear();
		vkDestroyCommandPool(device, computeCommandPool, null);
		
		vkDestroyShaderModule(device, prtUI.get(0).module(), null);
		vkDestroyShaderModule(device, prtUI.get(1).module(), null);
		memFree(prtUI);
		memFree(shEPName);
		
		vkDestroyDevice(device, null);
		if(ENABLE_VALIDATION_LAYERS) {
			if(vkGetInstanceProcAddr(instance, "vkDestroyDebugUtilsMessengerEXT") != NULL) {
				vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, null);
			}
		}
		vkDestroySurfaceKHR(instance, surface, null);
		vkDestroyInstance(instance, null);
		glfwDestroyWindow(window);
		glfwTerminate();
	}
	
	private static void createBuffer(long size, int usage, int props, LongBuffer pBuffer, LongBuffer pBufferMemory) {
		try(var stack = stackPush()){
			var bufferInfo = VkBufferCreateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
					.size(size)
					.usage(usage)
					.sharingMode(VK_SHARING_MODE_EXCLUSIVE);
			if(vkCreateBuffer(device, bufferInfo, null, pBuffer) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create buffer\n");
			}
			var memRequirements = VkMemoryRequirements.malloc(stack);
			vkGetBufferMemoryRequirements(device, pBuffer.get(0), memRequirements);
			
			var allocInfo = VkMemoryAllocateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
					.allocationSize(memRequirements.size())
					.memoryTypeIndex(findMemoryType(memRequirements.memoryTypeBits(), props));
			if(vkAllocateMemory(device, allocInfo, null, pBufferMemory) != VK_SUCCESS) {
				throw new RuntimeException("Failed to allocate buffer memory\n");
			}
			vkBindBufferMemory(device, pBuffer.get(0), pBufferMemory.get(0), 0);
		}
	}
	 
	private static void createImage(Image img, int memProps) {
		try(var stack = stackPush()){
			var imageInfo = VkImageCreateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
					.imageType(VK_IMAGE_TYPE_2D)
					.extent(VkExtent3D.calloc(stack)
							.width(img.width)
							.height(img.height)
							.depth(1))
					.mipLevels(1)
					.arrayLayers(1)
					.format(img.format)
					.tiling(img.tiling)
					.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
					.usage(img.usage)
					.samples(VK_SAMPLE_COUNT_1_BIT)
					.sharingMode(VK_SHARING_MODE_EXCLUSIVE);
			var pTextureImage = stack.mallocLong(1);
			var pTextureImageMemory = stack.mallocLong(1);
			if(vkCreateImage(device, imageInfo, null, pTextureImage) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create image\n");
			}
			var memRequirements = VkMemoryRequirements.malloc(stack);
			vkGetImageMemoryRequirements(device, pTextureImage.get(0), memRequirements);
			
			var allocInfo = VkMemoryAllocateInfo.calloc(stack)
					.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
					.allocationSize(memRequirements.size())
					.memoryTypeIndex(findMemoryType(memRequirements.memoryTypeBits(), memProps));
			if(vkAllocateMemory(device, allocInfo, null, pTextureImageMemory) != VK_SUCCESS) {
				throw new RuntimeException("Failed to allocate image memory\n");
			}
			
			img.image = pTextureImage.get(0);
			img.memory = pTextureImageMemory.get(0);
			
			vkBindImageMemory(device, img.image, img.memory, 0);
		}
	}
	
	private static void transitionImageLayout(Image img, int oldLayout, int newLayout) {
		try(var stack = stackPush()){
			var barrier = VkImageMemoryBarrier.calloc(1, stack)
					.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
					.oldLayout(oldLayout)
					.newLayout(newLayout)
					.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
					.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
					.image(img.image)
					.subresourceRange(VkImageSubresourceRange.calloc(stack)
							.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
							.baseMipLevel(0)
							.levelCount(1)
							.baseArrayLayer(0)
							.layerCount(1));
			int srcStage;
			int dstStage;
			
			if(oldLayout == VK_IMAGE_LAYOUT_UNDEFINED
					&& newLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {
				barrier.srcAccessMask(0)
						.dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
				srcStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
				dstStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
			} else if(oldLayout == VK_IMAGE_LAYOUT_GENERAL
					&& newLayout == VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL) {
				barrier.srcAccessMask(VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT)
						.dstAccessMask(VK_ACCESS_TRANSFER_READ_BIT);
				srcStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
				dstStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
			} else if(oldLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL
					&& newLayout == VK_IMAGE_LAYOUT_GENERAL) {
				barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT)
						.dstAccessMask(VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT | VK_ACCESS_TRANSFER_READ_BIT);
				srcStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
				dstStage = VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT | VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT | VK_PIPELINE_STAGE_TRANSFER_BIT;
			} else {
				throw new RuntimeException("Undupported layout transition\n");
			}
			
			singleTimeCommand(stack, (c, s) -> {
				vkCmdPipelineBarrier(c, srcStage, dstStage, 0, null, null, barrier);
			});
		}
	}
	
	static void drawingFrameBarrier(VkCommandBuffer cb, int idx) {
		try(var stack = stackPush()){
			var barrier = VkImageMemoryBarrier.calloc(1, stack);
			barrier.get(0)
					.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
					.oldLayout(VK_IMAGE_LAYOUT_GENERAL)
					.newLayout(VK_IMAGE_LAYOUT_GENERAL)
					.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
					.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
					.image(gBufferImages.get(idx).image)
					.subresourceRange(VkImageSubresourceRange.calloc(stack)
							.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
							.baseMipLevel(0)
							.levelCount(1)
							.baseArrayLayer(0)
							.layerCount(1));
			vkCmdPipelineBarrier(cb,
					VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT,
					VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT,
					0,
					null,
					null,
					barrier);
		}
	}
	
	private static void copyBuffer(long src, long dst, long size) {
		try(var stack = stackPush()){
			singleTimeCommand(stack, (c, s) -> {
				vkCmdCopyBuffer(c, src, dst, VkBufferCopy.calloc(1, s).size(size));
			});
		}
	}
	
	private static void copyBufferToImage(long buffer, Image img) {
		try(var stack = stackPush()){
			singleTimeCommand(stack, (c, s) -> {
				var region = VkBufferImageCopy.calloc(1, stack)
						.bufferOffset(0)
						.bufferRowLength(0)
						.bufferImageHeight(0)
						.imageSubresource(VkImageSubresourceLayers.calloc(s)
								.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
								.mipLevel(0)
								.baseArrayLayer(0)
								.layerCount(1))
						.imageOffset(VkOffset3D.calloc(s)
								.x(0)
								.y(0)
								.z(0))
						.imageExtent(VkExtent3D.calloc(s)
								.width(img.width)
								.height(img.height)
								.depth(1));
				vkCmdCopyBufferToImage(c, buffer, img.image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, region);
			});
		}
	}
	
	private static void memcpy(ByteBuffer dst, ByteBuffer src, long size) {
		src.limit((int)size);
		dst.put(src);
		src.limit(src.capacity()).rewind();
		dst.rewind();
	}
	
	private static void memcpy(IntBuffer dst, IntBuffer src, int size) {
		dst.put(src.limit(size)).rewind();
		src.limit(src.capacity()).rewind();
	}
	
	private static void singleTimeCommand(MemoryStack stack, BiConsumer<VkCommandBuffer, MemoryStack> fi) {
		var allocInfo = VkCommandBufferAllocateInfo.calloc(stack)
				.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
				.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
				.commandPool(computeCommandPool)
				.commandBufferCount(1);
		var pCommandBuffer = stack.mallocPointer(1);
		vkAllocateCommandBuffers(device, allocInfo, pCommandBuffer);
		var cb = new VkCommandBuffer(pCommandBuffer.get(0), device);
		
		var beginInfo = VkCommandBufferBeginInfo.calloc(stack)
				.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
				.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
		vkBeginCommandBuffer(cb, beginInfo);
		fi.accept(cb, stack);
		vkEndCommandBuffer(cb);
		
		var submitInfo = VkSubmitInfo.calloc(stack)
				.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
				.pCommandBuffers(pCommandBuffer);
		if(vkQueueSubmit(computeQueue, submitInfo, VK_NULL_HANDLE) != VK_SUCCESS) {
			throw new RuntimeException("Failed to copy command buffer\n");
		}
		vkQueueWaitIdle(computeQueue);
		vkFreeCommandBuffers(device, computeCommandPool, pCommandBuffer);
	}
	
	private static void beginSingleTimeCommand2(MemoryStack stack, Frame thisFrame) {
		var cb = thisFrame.cb;
		var beginInfo = VkCommandBufferBeginInfo.calloc(stack)
				.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
				.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
		vkBeginCommandBuffer(cb, beginInfo);
	}
	
	private static void endSingleTimeCommand2(MemoryStack stack, Frame thisFrame) {
		var cb = thisFrame.cb;
		vkEndCommandBuffer(cb);
		
		var submitInfo = VkSubmitInfo.calloc(stack)
				.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
				.waitSemaphoreCount(1)
				.pWaitSemaphores(stack.longs(thisFrame.imageAvailableSemaphore))
				.pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT))
				.pSignalSemaphores(stack.longs(thisFrame.renderFinishedSemaphore))
				.pCommandBuffers(stack.pointers(cb.address()));
		vkResetFences(device, stack.longs(thisFrame.fence));
		
		if(vkQueueSubmit(computeQueue, submitInfo, thisFrame.fence) != VK_SUCCESS) {
			throw new RuntimeException("Failed to submit command buffer\n");
		}
	}
	
	private static int findMemoryType(int typeFilter, int properties) {
		for(int i = 0; i < memProps.memoryTypeCount(); i++) {
			if((typeFilter & (1 << i)) != 0 && (memProps.memoryTypes(i).propertyFlags() & properties) == properties) {
				return i;
			}
		}
		throw new RuntimeException("Failed to find suitable memory type\n");
	}
	
	private static boolean checkValidationLayerSupport() {
		try(var stack = stackPush()) {
			var layerCount = stack.ints(0);
			vkEnumerateInstanceLayerProperties(layerCount, null);
			var availableLayers = VkLayerProperties.malloc(layerCount.get(0), stack);
			vkEnumerateInstanceLayerProperties(layerCount, availableLayers);
			var availableLayerNames = availableLayers.stream()
					.map(VkLayerProperties::layerNameString)
					.collect(toSet());
			return availableLayerNames.containsAll(VALIDATION_LAYERS);
		}
	}
	
	private static VkDebugUtilsMessengerCreateInfoEXT populateDebugMessengerCreateInfo(MemoryStack stack) {
		return VkDebugUtilsMessengerCreateInfoEXT.calloc(stack)
				.sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT)
				.messageSeverity(VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT
						| VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
				.messageType(VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT
						| VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT
						| VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT)
				.pfnUserCallback((int s, int t, long c, long u) -> {
					var callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(c);
					System.err.println("Validation Layer: "+ callbackData.pMessageString());
					return VK_FALSE;
				});
	}
	
	private static PointerBuffer getRequiredExtensions(MemoryStack stack) {
		var glfwExtensions = glfwGetRequiredInstanceExtensions();
		if(ENABLE_VALIDATION_LAYERS) {
			return stack.mallocPointer(glfwExtensions.capacity() + 1)
					.put(glfwExtensions)
					.put(stack.UTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME))
					.rewind();
		}
		return glfwExtensions;
	}
	
	private static PointerBuffer asPointerBuffer(MemoryStack stack, Collection<String> collection) {
		var buffer = stack.mallocPointer(collection.size());
		collection.stream()
				.map(stack::UTF8)
				.forEach(buffer::put);
		return buffer.rewind();
	}
}
