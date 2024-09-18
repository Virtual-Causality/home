package home.vcausality.launch;

import static home.vcausality.launch.LaunchRenderer3.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Consumer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.vulkan.VkCommandBuffer;

public class Launch {
	
	private static class Stat {
		private int code;
		private Runnable proc;
		private Consumer<Integer> onKeyPressed;
		private Runnable onKeyPressing;
		
		private Stat(int code, Runnable proc, Consumer<Integer> onKeyPressed, Runnable onKeyPressing) {
			this.code = code;
			this.proc = proc;
			this.onKeyPressed = onKeyPressed;
			this.onKeyPressing = onKeyPressing;
		}	
	}
	
	private static class SE {
		private byte[] buf;
		private int length;
		
		private SE(byte[] buf) {
			this.buf = buf;
		}
	}
	
	private static long window;
	
	private static ByteBuffer icon16;
	private static ByteBuffer icon32;
	private static ByteBuffer icon48;
	private static GLFWImage.Buffer icon;
	
	private static final String SHAFT_VERSION = "1.0.2";
	
	private static final int SEGMENT_CLOCK_IDX = 0;
	private static final int SEGMENT_LEVEL_IDX = 1;
	private static final int SEGMENT_WAIT_IDX = 2;
	private static final int SEGMENT_HP_IDX = 3;
	private static final int SEGMENT_SCORE_IDX = 4;
	private static final int SEGMENT_LINES_IDX = 5;
	private static final int SEGMENT_COUNT_IDX = 6;
	// private static final int SEGMENT_HPBAR_IDX = 7;
	private static final int SEGMENT_ICNT_IDX = 8;
	private static final int SEGMENT_TIME_H = 0;
	private static final int SEGMENT_TIME_H_DIGIT = 2;
	private static final int SEGMENT_TIME_M = 2;
	private static final int SEGMENT_TIME_M_DIGIT = 2;
	private static final int SEGMENT_TIME_S = 4;
	private static final int SEGMENT_TIME_S_DIGIT = 2;
	private static final int SEGMENT_LEVEL = 6;
	private static final int SEGMENT_LEVEL_DIGIT = 3;
	private static final int SEGMENT_WAIT_SEC = 9;
	private static final int SEGMENT_WAIT_SEC_DIGIT = 2;
	private static final int SEGMENT_WAIT_MILS = 11;
	private static final int SEGMENT_WAIT_MILS_DIGIT = 2;
	private static final int SEGMENT_HP_REM = 13;
	private static final int SEGMENT_HP_REM_DIGIT = 5;
	private static final int SEGMENT_HP_MAX = 18;
	private static final int SEGMENT_HP_MAX_DIGIT = 5;
	private static final int SEGMENT_SCORE = 23;
	private static final int SEGMENT_SCORE_DIGIT = 8;
	private static final int SEGMENT_LINES = 31;
	private static final int SEGMENT_LINES_DIGIT = 4;
	private static final int SEGMENT_BG = 35;
	private static final int SEGMENT_BG_DIGIT = 4;
	private static final int SEGMENT_T1 = 39;
	private static final int SEGMENT_T1_DIGIT = 4;
	private static final int SEGMENT_TG = 43;
	private static final int SEGMENT_TG_DIGIT = 4;
	
	private static final int GSTAT_G_S = 0x00000001;
	private static final int GSTAT_G_X = 0x00000002;
	private static final int GSTAT_G_N = 0x00000003;
	private static final int GSTAT_G_F = 0x00000004;
	
	private static final int GSTAT_P_N = 0xFFFFFFFF;
	private static final int GSTAT_P_D = 0xFFFFFFFE;
	private static final int GSTAT_P_S = 0xFFFFFFFD;
	private static final int GSTAT_P_P = 0xFFFFFFFC;
	
	// private static final int GSTAT_P_N_N = 0xFFF11100;
	private static final int GSTAT_T_L1 = 0xFFF11100;
	private static final int GSTAT_T_C1 = 0xFFF11101;
	private static final int GSTAT_T_I1 = 0xFFF11102;
	private static final int GSTAT_T_Q = 0xFFF11103;
	
	private static final int GSTAT_T_L1_N = 0xFFF21100;
	private static final int GSTAT_T_L2_C = 0xFFF21100;
	private static final int GSTAT_T_L2_S = 0xFFF21101;
	private static final int GSTAT_T_L2_G = 0xFFF21102;
	private static final int GSTAT_T_L2_P = 0xFFF21103;
	private static final int GSTAT_T_L2_N = 0xFFF21104;
	private static final int GSTAT_T_L2_M = 0xFFF21105;
	
	// private static final int GSTAT_T_C1_N = 0xFFF21200;
	private static final int GSTAT_T_C2_SEG = 0xFFF21200;
	private static final int GSTAT_T_C2_GRA = 0xFFF21201;
	private static final int GSTAT_T_C2_SOU = 0xFFF21202;
	private static final int GSTAT_T_C2_CTR = 0xFFF21203;
	
	// private static final int GSTAT_T_I1_N = 0xFFF21400;
	private static final int GSTAT_T_I2_ABO = 0xFFF21400;
	private static final int GSTAT_T_I2_SYS = 0xFFF21401;
	private static final int GSTAT_T_I2_LIC = 0xFFF21402;
	private static final int GSTAT_T_I2_STA = 0xFFF21403;
	
	private static final int CONTROL = 0;
	private static final int QUEUE_G = 16;
	private static final int BOARD_H = 32;
	private static final int BOARD_V = 88;
	private static final int QUEUE_0 = 480;
	private static final int QUEUE_1 = 496;
	private static final int QUEUE_2 = 512;
	private static final int QUEUE_3 = 528;
	private static final int QUEUE_4 = 544;
	private static final int STACK_0 = 560;
	private static final int STACK_1 = 576;
	private static final int STACK_2 = 592;
	
	// private static final int SEP_W = 4;
	private static final int OPN_W = 14;
	private static final int COL_H = 56;
	private static final int COL_V = 112;
	// private static final int FL_JUMP = 10;
	private static final int CELLS_NUM = 608;
	
	private static final int G_DN_DRP = 1 * 16;
	private static final int G_DP_ROT = 2 * 16;
	private static final int G_DP_MOV = 1 * 16;
	private static final int G_DN_ACC = 6 * 16;
	
	private static final int KEY_MVL = 0;
	private static final int KEY_MVR = 1;
	private static final int KEY_RTL = 2;
	private static final int KEY_RTR = 3;
	private static final int KEY_PSH = 4;
	private static final int KEY_POP = 5;
	private static final int KEY_DRP = 6;
	private static final int KEY_ACC = 7;
	private static final int KEY_SCR = 8;
	private static final int KEY_LOG = 9;
	private static final int KEY_ACT = 10;
	private static final int KEY_PAU = 11;
	private static final int KEY_CVL = 0;
	private static final int KEY_CVR = 1;
	private static final int KEY_RST = 2;
	private static final int KEY_NOP = 3;
	private static final int KEY_CVU = 4;
	private static final int KEY_CXL = 5;
	private static final int KEY_DEC = 6;
	private static final int KEY_CVD = 7;
	
	private static final int C_VA = 0;
	private static final int C_OC = 1;
	private static final int C_T1 = 2;
	private static final int C_T2 = 3;
	private static final int C_TG = 4;
	private static final int C_BM = 5;
	private static final int C_BG = 6;
	private static final int C_BF = 7;
	private static final int C_TYPES = 8;
	
	private static final int BAR_HP = 7;
	private static final int SCR_CSIZE = 0;
	private static final int SCR_FPS = 1;
	
	private static final int PLAYER_HP_X = 10000;
	
	private static final int D_BM = 3500;

	private static int[] kcda = {65, 68, 74, 75, 87, 76, 32, 83, 70, 71, 72, 256};
	private static int[] kcdb = {263, 262, 68, 70, 265, 71, 32, 264, 81, 87, 69, 256};
	private static String[] gcda = {"A", "D", "J", "K", "W", "L", "SPC", "S", "F", "G", "H", "ESC"};
	private static String[] gcdb = {"<-", "->", "D", "F", "UP", "G", "SPC", "DN", "Q", "W", "E", "ESC"};
	
	private static String[] cellG = {".", "#", "S", "?", "$", "@", "O", "X"};
	
	private static final String CFG_NAME = "assets/config.txt";
	
	private static int[] comp = {1, 2, 4, 8};
	private static int[] rate = {0, 100, 500, 50, 8000, 0, 1000, 10};
	private static int[] heal = {0, 2, 10, 1, 40, 0, 20, 1};
	private static int[] cdmg = {40, 15, 25, 20, 100, 0, 30, 500};
	private static int[] ldnc = {
			201 * 16, 191 * 16, 181 * 16, 171 * 16, 161 * 16,
			151 * 16, 141 * 16, 131 * 16, 121 * 16, 111 * 16,
			101 * 16, 91 * 16, 81 * 16, 71 * 16, 61 * 16,
			51 * 16, 41 * 16, 21 * 16, 121 * 16, 121 * 16, 2 * 16
	};
	private static int[] ltoc = {
			208, 208, 208, 208, 208, 208, 192, 192, 192, 192, 176, 176, 176, 176, 160, 160, 160, 144, 256, 0, 128
	};
	private static int[] ltt1 = {
			240, 240, 240, 232, 232, 232, 224, 224, 224, 224, 216, 216, 216, 216, 208, 208, 208, 192, 512, 64, 160
	};
	private static int[] lsta = {0, 3, 6, 9, 0, 20};
	private static int[] llim = {6, 10, 14, 18, 22, 21};
	private static int[] lnxt = {8, 8, 7, 7, -1, 2};
	private static int[] sgdf = {1, 1, 1, 1, 1, 1, 1, 2, 2};
	private static int[] sglm = {2, 2, 2, 2, 2, 2, 2, 3, 3};
	private static int[] scdf = {0, 1};
	private static int[] sclm = {2, 2};
	private static int[] sodf = {100};
	private static int[] solm = {101};
	private static int[] ctdf = {0};
	private static int[] ctlm = {2};
	
	private static int[] gposg = {
			0, 1, 8, 9, 2, 3, 6, 7, 12, 13, 14, 10
	};
	private static int[] gposp = {
			0, 1, 17, 18, 4, 16, 15, 5, 12, 13, 14, 11
	};
	
	private static int[] kcla = {
			0, 0, 1, 0, 6, 0, 7, 0, 11, 0, 8, 0, 17, 0, 10, 0, 3, 0, 4, 0, 5, 0, 16, 0
	};
	private static int[] kclb = {
			12, 0, 15, 0, 1, 0, 3, 0, 13, 0, 4, 0, 17, 0, 14, 0, 9, 0, 11, 0, 2, 0, 16, 0
	};
	
	private static int[] wsizex = {800, 1600};
	private static int[] wsizey = {900, 1800};
	
	private static String title = "Shaft";
	
	private static Stat gstat_g_s;
	private static Stat gstat_g_x;
	private static Stat gstat_g_n;
	private static Stat gstat_g_f;
	
	private static Stat gstat_p_n;
	private static Stat gstat_p_d;
	private static Stat gstat_p_s;
	private static Stat gstat_p_p;
	
	private static Stat gstat_t_l1;
	private static Stat gstat_t_c1;
	private static Stat gstat_t_i1;
	private static Stat gstat_t_q;
	
	private static Stat gstat_t_l2_c;
	private static Stat gstat_t_l2_s;
	private static Stat gstat_t_l2_g;
	private static Stat gstat_t_l2_p;
	private static Stat gstat_t_l2_n;
	private static Stat gstat_t_l2_m;
	
	private static Stat gstat_t_c2_seg;
	private static Stat gstat_t_c2_gra;
	private static Stat gstat_t_c2_sou;
	private static Stat gstat_t_c2_ctr;
	
	private static Stat gstat_t_i2_abo;
	private static Stat gstat_t_i2_sys;
	private static Stat gstat_t_i2_lic;
	private static Stat gstat_t_i2_sta;
	
	private static int[] kflg = new int[kcda.length];
	// private static int[] kgot = new int[kcda.length];
	private static int[] kcde = new int[kcda.length];
	private static int[] kcle = new int[kcla.length];
	private static String[] gcde = new String[gcda.length];
	private static int[] gpos = new int[gposp.length];
	
	private static int[] sgsw = new int[sgdf.length];
	private static int[] scsw = new int[scdf.length];
	private static int[] sosw = new int[sodf.length];
	private static int[] ctsw = new int[ctdf.length];
	
	private static int[] cells = new int[CELLS_NUM];
	private static int[] segment = new int[48];
	
	private static int[] ccnt = new int[C_TYPES];
	private static int[] keep = new int[5];
	
	private static int c_sw;
	private static int c_x;
	private static int c_y;
	private static int dn_cnt;
	private static int dn_max;
	private static int score;
	private static int s_cnt;
	private static int l_cnt;
	private static int l_loc;
	private static int g_level;
	private static int g_mode;
	private static int hp = PLAYER_HP_X;
	
	private static int cur_v;
	private static int cur_h;
	private static int cur_k;
	private static int fx_cnt;
	
	private static int i_t1_color;
	private static int i_t2_color;
	private static int i_tg_color;
	private static int i_bar_color;
	
	private static int color_cnt;
	private static int color_i;
	private static int monoc_cnt;
	private static int monoc_i;
	
	private static long prevTime;
	private static long diff;
	
	private static boolean qg_isVacant;
	private static boolean hzd;
	
	private static boolean ir_open;
	private static boolean cfg_sel;
	
	private static boolean drawFlg;
	
	private static boolean logRequested;
	private static boolean actRequested;
	
	private static Stat g_stat;
	private static Stat p_stat;
	
	private static List<ShaderUBOInfo> regions;
	private static ShaderUBOInfo SCREEN;
	private static ShaderUBOInfo[] PANELS;
	private static ShaderUBOInfo[] SHADOWS;
	private static ShaderUBOInfo MENU;
	private static ShaderUBOInfo BG_CNT_T1;
	private static ShaderUBOInfo BG_CNT_TG;
	private static ShaderUBOInfo BG_HP_BAR;
	
	private static ShaderUBOInfo LOCAL;
	
	private static SourceDataLine[] sdl;
	private static int lineIdx;
	
	private static FloatControl[] sec;
	
	
	private static SE SE_CV;
	private static SE SE_DEC;
	private static SE SE_CXL;
	private static SE SE_NUL;
	private static SE SE_CTRL;
	private static SE SE_STACK;
	private static SE SE_STACK2;
	private static SE SE_LAND;
	private static SE SE_CLEAR;
	private static SE SE_GMOVR;
	private static SE SE_LVUP;
	
	private static Consumer<Integer> k_p_n_e = (k) -> {
		if(k == kcde[KEY_CVU]) {
			play(SE_CV);
			cur_v--;
			if(cur_v == -1) {
				cur_v = 3;
			}
		} else if(k == kcde[KEY_CVD]) {
			play(SE_CV);
			cur_v++;
			if(cur_v == 4) {
				cur_v = 0;
			}
		} else if(k == kcde[KEY_DEC]) {
			play(SE_DEC);
			p_stat = g_stat;
			switch(cur_v) {
			case 0:
				g_stat = gstat_t_l1;
				break;
			case 1:
				g_stat = gstat_t_c1;
				break;
			case 2:
				g_stat = gstat_t_i1;
				break;
			case 3:
				g_stat = gstat_t_q;
				break;
			}
			cur_v = 0;
		} else if(k == kcde[KEY_CXL]) {
			play(SE_CXL);
			cur_v = 0;
		}
	};
	
	private static Consumer<Integer> k_p_d_e = (k) -> {
		if(k == kcde[KEY_CVU]) {
			play(SE_CV);
			cur_v--;
			if(cur_v == -1) {
				cur_v = 1;
			}
		} else if(k == kcde[KEY_CVD]) {
			play(SE_CV);
			cur_v++;
			if(cur_v == 2) {
				cur_v = 0;
			}
		} else if(k == kcde[KEY_DEC]) {
			play(SE_DEC);
			switch(cur_v) {
			case 0:
				p_stat = gstat_p_n;
				g_stat = gstat_p_n;
				g_level = 0;
				g_mode = 0;
				initGame();
				barColor();
				break;
			case 1:
				g_stat = gstat_t_q;
				break;
			}
			cur_v = 0;
		} else if(k == kcde[KEY_CXL]) {
			play(SE_CXL);
			cur_v = 0;
		}
	};
	
	private static Consumer<Integer> k_p_s_e = (k) -> {
		if(k == kcde[KEY_CVU]) {
			play(SE_CV);
			cur_v--;
			if(cur_v == -1) {
				cur_v = 2;
			}
		} else if(k == kcde[KEY_CVD]) {
			play(SE_CV);
			cur_v++;
			if(cur_v == 3) {
				cur_v = 0;
			}
		} else if(k == kcde[KEY_DEC]) {
			play(SE_DEC);
			switch(cur_v) {
			case 0:
				p_stat = gstat_p_s;
				g_stat = gstat_t_i2_sta;
				break;
			case 1:
				p_stat = gstat_p_n;
				g_stat = gstat_p_n;
				g_level = 0;
				g_mode = 0;
				initGame();
				barColor();
				break;
			case 2:
				g_stat = gstat_t_q;
				break;
			}
			cur_v = 0;
		} else if(k == kcde[KEY_CXL]) {
			play(SE_CXL);
			cur_v = 0;
		}
	};
	
	private static Consumer<Integer> k_p_p_e = (k) -> {
		if(k == kcde[KEY_CVU]) {
			play(SE_CV);
			cur_v--;
			if(cur_v == -1) {
				cur_v = 4;
			}
		} else if(k == kcde[KEY_CVD]) {
			play(SE_CV);
			cur_v++;
			if(cur_v == 5) {
				cur_v = 0;
			}
		} else if(k == kcde[KEY_DEC]) {
			play(SE_DEC);
			switch(cur_v) {
			case 0:
				g_stat = p_stat;
				cur_v = 0;
				break;
			case 1:
				g_stat = gstat_t_c1;
				cur_v = 0;
				break;
			case 2:
				p_stat = gstat_p_n;
				g_stat = gstat_p_n;
				g_level = 0;
				g_mode = 0;
				cur_v = 0;
				initGame();
				barColor();
				break;
			case 3:
				g_stat = gstat_t_i1;
				cur_v = 0;
				break;
			case 4:
				g_stat = gstat_t_q;
				break;
			}
			cur_v = 0;
		} else if(k == kcde[KEY_CXL]) {
			play(SE_CXL);
			cur_v = 0;
		} else if(k == kcde[KEY_PAU]) {
			play(SE_DEC);
			g_stat = p_stat;
			cur_v = 0;
		}
	};
	
	private static Consumer<Integer> k_t_l1_e = (k) -> {
		if(k == kcde[KEY_CVU]) {
			play(SE_CV);
			cur_v--;
			if(cur_v == -1) {
				if(ir_open) {
					cur_v = 5;
				} else {
					cur_v = 4;
				}
			}
			g_level = lsta[cur_v];
			g_mode = cur_v;
		} else if(k == kcde[KEY_CVD]) {
			play(SE_CV);
			cur_v++;
			if(ir_open) {
				if(cur_v == 6) {
					cur_v = 0;
				}
			} else {
				if(cur_v == 5) {
					cur_v = 0;
				}
			}
			g_level = lsta[cur_v];
			g_mode = cur_v;
		} else if(k == kcde[KEY_DEC]) {
			play(SE_DEC);
			switch(cur_v) {
			case 0:
				g_stat = gstat_t_l2_c;
				break;
			case 1:
				g_stat = gstat_t_l2_s;
				break;
			case 2:
				g_stat = gstat_t_l2_g;
				break;
			case 3:
				g_stat = gstat_t_l2_p;
				break;
			case 4:
				g_stat = gstat_t_l2_n;
				break;
			case 5:
				g_stat = gstat_t_l2_m;
				break;
			}
		} else if(k == kcde[KEY_CXL]) {
			play(SE_CXL);
			g_level = 0;
			g_stat = gstat_p_n;
			ir_open = false;
			cur_v = 0;
		}
	};
	
	private static Consumer<Integer> k_t_c1_e = (k) -> {
		if(k == kcde[KEY_CVU]) {
			play(SE_CV);
			cur_v--;
			if(cur_v == -1) {
				cur_v = 3;
			}
		} else if(k == kcde[KEY_CVD]) {
			play(SE_CV);
			cur_v++;
			if(cur_v == 4) {
				cur_v = 0;
			}
		} else if(k == kcde[KEY_DEC]) {
			play(SE_DEC);
			switch(cur_v) {
			case 0:
				g_stat = gstat_t_c2_seg;
				break;
			case 1:
				g_stat = gstat_t_c2_gra;
				break;
			case 2:
				g_stat = gstat_t_c2_sou;
				break;
			case 3:
				g_stat = gstat_t_c2_ctr;
				break;
			}
			cur_v = 0;
		} else if(k == kcde[KEY_CXL]) {
			play(SE_CXL);
			if(p_stat.code > 0) {
				g_stat = gstat_p_p;
			} else {
				g_stat = gstat_p_n;
			}
			cur_v = 0;
		}
	};
	
	private static Consumer<Integer> k_t_i1_e = (k) -> {
		if(k == kcde[KEY_CVU]) {
			play(SE_CV);
			cur_v--;
			if(cur_v == -1) {
				cur_v = 3;
			}
		} else if(k == kcde[KEY_CVD]) {
			play(SE_CV);
			cur_v++;
			if(cur_v == 4) {
				cur_v = 0;
			}
		} else if(k == kcde[KEY_DEC]) {
			play(SE_DEC);
			switch(cur_v) {
			case 0:
				i_t1_color = selColor();
				i_t2_color = selColor();
				i_tg_color = selColor();
				g_stat = gstat_t_i2_abo;
				break;
			case 1:
				g_stat = gstat_t_i2_sys;
				break;
			case 2:
				g_stat = gstat_t_i2_lic;
				break;
			case 3:
				g_stat = gstat_t_i2_sta;
				break;
			}
			cur_v = 0;
		} else if(k == kcde[KEY_CXL]) {
			play(SE_CXL);
			if(p_stat.code > 0) { // <---   "> 0" 1.5.0~
				g_stat = gstat_p_p;
			} else {
				g_stat = gstat_p_n;
			}
			cur_v = 0;
		}
	};
	
	private static Consumer<Integer> k_t_l2_com_e = (k) -> {
		if(k == kcde[KEY_DEC]) {
			play(SE_DEC);
			initGame();
			g_stat = gstat_g_s;
			p_stat = gstat_g_s;
			cur_v = 0;
			ir_open = false;
			gen_i();
		} else if(k == kcde[KEY_CXL]) {
			play(SE_CXL);
			g_stat = gstat_t_l1;
		}
	};
	
	private static Consumer<Integer> k_t_l2_n_e = (k) -> {
		if(k == kcde[KEY_CVL]) {
			play(SE_CV);
			cur_h--;
			if(cur_h == -1) {
				cur_h = 19;
			}
			g_level = cur_h;
		} else if(k == kcde[KEY_CVR]) {
			play(SE_CV);
			cur_h++;
			if(cur_h == 20) {
				cur_h = 0;
			}
			g_level = cur_h;
		} else if(k == kcde[KEY_DEC]) {
			play(SE_DEC);
			initGame();
			g_stat = gstat_g_s;
			p_stat = gstat_g_s;
			cur_v = 0;
			cur_h = 0;
			ir_open = false;
			gen_i();
		} else if(k == kcde[KEY_CXL]) {
			if(cur_h == 17) {
				play(SE_LVUP);
				ir_open = true;
			} else {
				play(SE_CXL);
			}
			g_stat = gstat_t_l1;
			cur_h = 0;
			g_level = lsta[cur_v];
		}
	};
	
	private static Consumer<Integer> k_t_c2_seg_e = (k) -> {
		if(cfg_sel) {
			if(k == kcde[KEY_CVL]) {
				play(SE_CV);
				cur_h--;
				if(cur_h == -1) {
					cur_h = sglm[cur_v] - 1;
				}
				sgsw[cur_v] = cur_h;
				if(cur_v == BAR_HP) {
					barColor();
				}
			} else if(k == kcde[KEY_CVR]) {
				play(SE_CV);
				cur_h++;
				if(cur_h == sglm[cur_v]) {
					cur_h = 0;
				}
				sgsw[cur_v] = cur_h;
				if(cur_v == BAR_HP) {
					barColor();
				}
			} else if(k == kcde[KEY_DEC]) {
				play(SE_DEC);
				cur_h = 0;
				cfg_sel = false;
				if(cur_v == BAR_HP) {
					barColor();
				}
			} else if(k == kcde[KEY_CXL]) {
				play(SE_CXL);
				sgsw[cur_v] = cur_k;
				cur_h = 0;
				cfg_sel = false;
				if(cur_v == BAR_HP) {
					barColor();
				}
			} else if(k == kcde[KEY_RST]) {
				play(SE_CV);
				cur_h = sgdf[cur_v];
				sgsw[cur_v] = sgdf[cur_v];
				if(cur_v == BAR_HP) {
					barColor();
				}
			}
		} else {
			if(k == kcde[KEY_CVU]) {
				play(SE_CV);
				cur_v--;
				if(cur_v == -1) {
					cur_v = sglm.length - 1;
				}
			} else if(k == kcde[KEY_CVD]) {
				play(SE_CV);
				cur_v++;
				if(cur_v == sglm.length) {
					cur_v = 0;
				}
			} else if(k == kcde[KEY_DEC]) {
				play(SE_DEC);
				cfg_sel = true;
				cur_h = sgsw[cur_v];
				cur_k = sgsw[cur_v];
			} else if(k == kcde[KEY_CXL]) {
				play(SE_CXL);
				g_stat = gstat_t_c1;
				cur_v = 0;
			}
		}
	};
	
	private static Consumer<Integer> k_t_c2_gra_e = (k) -> {
		if(cfg_sel) {
			if(k == kcde[KEY_CVL]) {
				play(SE_CV);
				cur_h--;
				if(cur_h == -1) {
					cur_h = sclm[cur_v] - 1;
				}
				scsw[cur_v] = cur_h;
				switch(cur_v) {
				case SCR_CSIZE:
					requestResize(wsizex[scsw[cur_v]], wsizey[scsw[cur_v]]);
					break;
				case SCR_FPS:
					requestVSync(scsw[cur_v] == 1);
					break;
				}
			} else if(k == kcde[KEY_CVR]) {
				play(SE_CV);
				cur_h++;
				if(cur_h == sclm[cur_v]) {
					cur_h = 0;
				}
				scsw[cur_v] = cur_h;
				switch(cur_v) {
				case SCR_CSIZE:
					requestResize(wsizex[scsw[cur_v]], wsizey[scsw[cur_v]]);
					break;
				case SCR_FPS:
					requestVSync(scsw[cur_v] == 1);
					break;
				}
			} else if(k == kcde[KEY_DEC]) {
				play(SE_DEC);
				cur_h = 0;
				cfg_sel = false;
			} else if(k == kcde[KEY_CXL]) {
				play(SE_CXL);
				sgsw[cur_v] = cur_k;
				// SCREEN
				cur_h = 0;
				cfg_sel = false;
				switch(cur_v) {
				case SCR_CSIZE:
					requestResize(wsizex[scsw[cur_v]], wsizey[scsw[cur_v]]);
					break;
				case SCR_FPS:
					requestVSync(scsw[cur_v] == 1);
					break;
				}
			} else if(k == kcde[KEY_RST]) {
				play(SE_CV);
				cur_h = scdf[cur_v];
				scsw[cur_v] = scdf[cur_v];
				switch(cur_v) {
				case SCR_CSIZE:
					requestResize(wsizex[scsw[cur_v]], wsizey[scsw[cur_v]]);
					break;
				case SCR_FPS:
					requestVSync(scsw[cur_v] == 1);
					break;
				}
			}
		} else {
			if(k == kcde[KEY_CVU]) {
				play(SE_CV);
				cur_v--;
				if(cur_v == -1) {
					cur_v = sclm.length - 1;
				}
			} else if(k == kcde[KEY_CVD]) {
				play(SE_CV);
				cur_v++;
				if(cur_v == sclm.length) {
					cur_v = 0;
				}
			} else if(k == kcde[KEY_DEC]) {
				play(SE_DEC);
				cfg_sel = true;
				cur_h = scsw[cur_v];
				cur_k = scsw[cur_v];
			} else if(k == kcde[KEY_CXL]) {
				play(SE_CXL);
				g_stat = gstat_t_c1;
				cur_v = 0;
			}
		}
	};
	
	private static Consumer<Integer> k_t_c2_sou_e = (k) -> {
		if(cfg_sel) {
			if(k == kcde[KEY_CVL]) {
				if(kflg[KEY_NOP] == 1) {
					cur_h--;
					if(cur_h == -1) {
						play(SE_NUL);
						cur_h = 0;
					} else {
						play(SE_CV);
					}
					sosw[cur_v] = cur_h;
					volume(sosw[cur_v]);
				}
			} else if(k == kcde[KEY_CVR]) {
				if(kflg[KEY_NOP] == 1) {
					cur_h++;
					if(cur_h >= solm[cur_v]) {
						play(SE_NUL);
						cur_h = solm[cur_v] - 1;
					} else {
						play(SE_CV);
					}
					sosw[cur_v] = cur_h;
					volume(sosw[cur_v]);
				}
			} else if(k == kcde[KEY_DEC]) {
				play(SE_DEC);
				cur_h = 0;
				cfg_sel = false;
			} else if(k == kcde[KEY_CXL]) {
				play(SE_CXL);
				sosw[cur_v] = cur_k;
				volume(sosw[cur_v]);
				cur_h = 0;
				cfg_sel = false;
			} else if(k == kcde[KEY_RST]) {
				play(SE_CV);
				cur_h = sodf[cur_v];
				sosw[cur_v] = sodf[cur_v];
				volume(sosw[cur_v]);
			}
		} else {
			if(k == kcde[KEY_CVU]) {
				play(SE_CV);
				cur_v--;
				if(cur_v == -1) {
					cur_v = solm.length - 1;
				}
			} else if(k == kcde[KEY_CVD]) {
				play(SE_CV);
				cur_v++;
				if(cur_v == solm.length) {
					cur_v = 0;
				}
			} else if(k == kcde[KEY_DEC]) {
				play(SE_DEC);
				cfg_sel = true;
				cur_h = sosw[cur_v];
				cur_k = sosw[cur_v];
			} else if(k == kcde[KEY_CXL]) {
				play(SE_CXL);
				g_stat = gstat_t_c1;
				cur_v = 0;
			}
		}
	};
	
	private static Consumer<Integer> k_t_c2_ctr_e = (k) -> {
		if(cfg_sel) {
			if(k == kcde[KEY_CVL]) {
				play(SE_CV);
				cur_h--;
				if(cur_h == -1) {
					cur_h = ctlm[cur_v] - 1;
				}
			} else if(k == kcde[KEY_CVR]) {
				play(SE_CV);
				cur_h++;
				if(cur_h == ctlm[cur_v]) {
					cur_h = 0;
				}
			} else if(k == kcde[KEY_DEC]) {
				play(SE_DEC);
				// CONTROL CHANGE
				if(cur_h == 0) {
					for(int i = 0; i < kcde.length; i++) {
						kcde[i] = kcda[i];
						kflg[i] = 0;
						kcle[i << 1] = kcla[i << 1];
						kcle[(i << 1) | 1] = 0;
						gcde[i] = gcda[i];
					}
				} else {
					for(int i = 0; i < kcde.length; i++) {
						kcde[i] = kcdb[i];
						kflg[i] = 0;
						kcle[i << 1] = kclb[i << 1];
						kcle[(i << 1) | 1] = 0;
						gcde[i] = gcdb[i];
					}
				}
				ctsw[cur_v] = cur_h;
				cur_h = 0;
				cfg_sel = false;
			} else if(k == kcde[KEY_CXL]) {
				play(SE_CXL);
				cur_h = 0;
				cfg_sel = false;
			} else if(k == kcde[KEY_RST]) {
				play(SE_CV);
				cur_h = ctdf[cur_v];
			}
		} else {
			if(k == kcde[KEY_CVU]) {
				play(SE_CV);
				cur_v--;
				if(cur_v == -1) {
					cur_v = ctlm.length -1;
				}
			} else if(k == kcde[KEY_CVD]) {
				play(SE_CV);
				cur_v++;
				if(cur_v == ctlm.length) {
					cur_v = 0;
				}
			} else if(k == kcde[KEY_DEC]) {
				play(SE_DEC);
				cfg_sel = true;
				cur_h = ctsw[cur_v];
			} else if(k == kcde[KEY_CXL]) {
				play(SE_CXL);
				g_stat = gstat_t_c1;
				cur_v = 0;
			}
		}
	};
	
	private static Consumer<Integer> k_t_i2_abo_e = (k) -> {
		 if(k == kcde[KEY_CVL]) {
			 cur_h--;
			 if(cur_h == -1) {
				 play(SE_NUL);
				 cur_h = 0;
			 } else {
				 play(SE_CV);
			 }
		 } else if(k == kcde[KEY_CVR]) {
			 cur_h++;
			 if(cur_h == 3) {
				 play(SE_NUL);
				 cur_h = 2;
			 } else {
				 play(SE_CV);
			 }
		 } else if(k == kcde[KEY_DEC]) {
			 play(SE_CXL);
			 cur_h = 0;
			 g_stat = gstat_t_i1;
		 } else if(k == kcde[KEY_CXL]) {
			 play(SE_CXL);
			 cur_h = 0;
			 g_stat = gstat_t_i1;
		 }
	};
	
	private static Consumer<Integer> k_t_i2_sys_e = (k) -> {
		if(k == kcde[KEY_DEC]) {
			play(SE_CXL);
			g_stat = gstat_t_i1;
		} else if(k == kcde[KEY_CXL]) {
			play(SE_CXL);
			g_stat = gstat_t_i1;
		}
	};
	
	private static Consumer<Integer> k_t_i2_lic_e = (k) -> {
		if(k == kcde[KEY_DEC]) {
			play(SE_CXL);
			g_stat = gstat_t_i1;
		} else if(k == kcde[KEY_CXL]) {
			play(SE_CXL);
			g_stat = gstat_t_i1;
		}
	};
	
	private static Consumer<Integer> k_t_i2_sta_e = (k) -> {
		if(k == kcde[KEY_DEC]) {
			if(p_stat.code == GSTAT_P_S) {
				play(SE_DEC);
				g_stat = gstat_p_n;
				p_stat = gstat_p_n;
				g_level = 0;
				g_mode = 0;
				initGame();
				barColor();
			} else {
				play(SE_CXL);
				g_stat = gstat_t_i1;
			}
		} else if(k == kcde[KEY_CXL]) {
			if(p_stat.code != GSTAT_P_S) {
				play(SE_CXL);
				g_stat = gstat_t_i1;
			}
		}
	};
	
	private static Consumer<Integer> k_g_nf_e = (k) -> {
		if(k == kcde[KEY_MVL]) {
			mov_l(true);
		} else if(k == kcde[KEY_MVR]) {
			mov_r(true);
		} else if(k == kcde[KEY_RTL]) {
			rot_l(true);
		} else if(k == kcde[KEY_RTR]) {
			rot_r(true);
		} else if(k == kcde[KEY_PSH]) {
			push();
		} else if(k == kcde[KEY_POP]) {
			pop();
		} else if(k == kcde[KEY_DRP]) {
			mov_dn();
			dn_max = G_DN_DRP;
			dn_cnt = G_DN_DRP;
		} else if(k == kcde[KEY_PAU]) {
			p_stat = g_stat;
			g_stat = gstat_p_p;
		}
	};
	
	private static Consumer<Integer> k_a_nop_e = (k) -> {};
	
	private static Runnable k_t_c2_sou_g = () -> {
		if(cfg_sel && kflg[KEY_NOP] == 0) {
			if(kflg[KEY_CVL] == 1) {
				cur_h--;
				if(cur_h == -1) {
					cur_h = 0;
				}
				sosw[cur_v] = cur_h;
				volume(sosw[cur_v]);
			} else if(kflg[KEY_CVR] == 1) {
				cur_h++;
				if(cur_h == solm[cur_v]) {
					cur_h = solm[cur_v] - 1;
				}
				sosw[cur_v] = cur_h;
				volume(sosw[cur_v]);
			}
		}
	};
	
	private static Runnable k_g_n_g = () -> {
		if(kflg[KEY_ACC] == 1) {
			if(dn_cnt > G_DN_ACC) {
				dn_cnt = G_DN_ACC;
			}
		}
	};
	
	private static Runnable k_a_nop_g = () -> {};
	
	private static Runnable p_p_q = () -> {
		requestGameClose();
	};
	
	private static Runnable p_pa = () -> {
		drawFlg = false;
	};
	
	private static Runnable p_g_s = () -> {
		dn_cnt = G_DN_ACC;
		dn_max = ldnc[g_level];
		dept();
		mov_dn();
		g_stat = gstat_g_n;
	};
	
	private static Runnable p_g_n = () -> {
		dn_cnt -= diff;
		if(dn_cnt <= 0) {
			dn_cnt = dn_max;
			mov_dn();
		}
		
	};
	
	private static Runnable p_g_f = () -> {
		dn_cnt -= diff;
		if(dn_cnt <= 0) {
			play(SE_LAND);
			dr_ctrl();
			int[] res = judge();
			if(res[4] > 0) {
				fx_cnt = 20 * 16;
				g_stat = gstat_g_x;
				for(int i = 0; i < 5; i++) {
					keep[i] = res[i];
				}
			} else {
				del_drop(res);
				g_stat = gstat_g_s;
				if(hzd) {
					calc_dmg();
				}
				hzd = false;
				dn_cnt = ldnc[g_level];
				dn_max = ldnc[g_level];
			}
			drawFlg = false;
		}
	};
	
	private static Runnable p_g_x = () -> {
		fx_cnt -= diff;
		if(fx_cnt <= 0) {
			calc_score(keep);
			del_drop(keep);
			g_stat = gstat_g_s;
			if(hzd) {
				calc_dmg();
			}
			hzd = false;
			if(lnxt[g_mode] > 0 && l_loc >= lnxt[g_mode]) {
				l_loc = l_loc - lnxt[g_mode];
				g_level++;
				if(g_level == llim[g_mode]) {
					play(SE_CLEAR);
					g_stat = gstat_p_s;
					g_level--;
				} else {
					play(SE_LVUP);
				}
			} else {
				play(SE_LAND);
			}
			dn_cnt = ldnc[g_level];
			dn_max = ldnc[g_level];
		}
		drawFlg = false;
	};
	
	static void initArrays() {
		var f = new File(CFG_NAME);
		boolean c = false;
		if(f.exists() && f.canRead()) {
			try(var sc = new Scanner(f)){
				for(int i = 0; i < sgdf.length; i++) {
					int j = sc.nextInt();
					sgsw[i] = j >= 0 && j < sglm[i] ? j : sgdf[i];
				}
				for(int i = 0; i < scdf.length; i++) {
					int j = sc.nextInt();
					scsw[i] = j >= 0 && j < sclm[i] ? j : scdf[i];
				}
				for(int i = 0; i < sodf.length; i++) {
					int j = sc.nextInt();
					sosw[i] = j >= 0 && j < solm[i] ? j : sodf[i];
				}
				for(int i = 0; i < ctdf.length; i++) {
					int j = sc.nextInt();
					ctsw[i] = j >= 0 && j < ctlm[i] ? j : ctdf[i];
				}
				
			} catch (IOException | NoSuchElementException | IllegalStateException e) {
				e.printStackTrace();
				c = true;
			}
		} else {
			c = true;
		}
		
		if(c) {
			for(int i = 0; i < sgsw.length; i++) {sgsw[i] = sgdf[i];}
			for(int i = 0; i < scsw.length; i++) {scsw[i] = scdf[i];}
			for(int i = 0; i < sosw.length; i++) {sosw[i] = sodf[i];}
			for(int i = 0; i < ctsw.length; i++) {ctsw[i] = ctdf[i];}
		}
		if(ctsw[0] == 0) {
			for(int i = 0; i < kcde.length; i++) {kcde[i] = kcda[i];}
			for(int i = 0; i < kcle.length; i++) {kcle[i] = kcla[i];}
			for(int i = 0; i < gcde.length; i++) {gcde[i] = gcda[i];}
		} else {
			for(int i = 0; i < kcde.length; i++) {kcde[i] = kcdb[i];}
			for(int i = 0; i < kcle.length; i++) {kcle[i] = kclb[i];}
			for(int i = 0; i < gcde.length; i++) {gcde[i] = gcdb[i];}
		}
		
		requestResize(wsizex[scsw[0]], wsizey[scsw[0]]);
		requestVSync(scsw[1] == 1);
		
		for(int i = 0; i < gpos.length; i++) {gpos[i] = gposp[i];}
	}
	
	static long initSystem() {
		gstat_g_s = new Stat(GSTAT_G_S, p_g_s, k_a_nop_e, k_a_nop_g);
		gstat_g_x = new Stat(GSTAT_G_X, p_g_x, k_a_nop_e, k_a_nop_g);
		gstat_g_n = new Stat(GSTAT_G_N, p_g_n, k_g_nf_e, k_g_n_g);
		gstat_g_f = new Stat(GSTAT_G_F, p_g_f, k_g_nf_e, k_a_nop_g);
		gstat_p_n = new Stat(GSTAT_P_N, p_pa, k_p_n_e, k_a_nop_g);
		gstat_p_d = new Stat(GSTAT_P_D, p_pa, k_p_d_e, k_a_nop_g);
		gstat_p_s = new Stat(GSTAT_P_S, p_pa, k_p_s_e, k_a_nop_g);
		gstat_p_p = new Stat(GSTAT_P_P, p_pa, k_p_p_e, k_a_nop_g);
		gstat_t_l1 = new Stat(GSTAT_T_L1, p_pa, k_t_l1_e, k_a_nop_g);
		gstat_t_c1 = new Stat(GSTAT_T_C1, p_pa, k_t_c1_e, k_a_nop_g);
		gstat_t_i1 = new Stat(GSTAT_T_I1, p_pa, k_t_i1_e, k_a_nop_g);
		gstat_t_q = new Stat(GSTAT_T_Q, p_p_q, k_a_nop_e, k_a_nop_g);
		gstat_t_l2_c = new Stat(GSTAT_T_L2_C, p_pa, k_t_l2_com_e, k_a_nop_g);
		gstat_t_l2_s = new Stat(GSTAT_T_L2_S, p_pa, k_t_l2_com_e, k_a_nop_g);
		gstat_t_l2_g = new Stat(GSTAT_T_L2_G, p_pa, k_t_l2_com_e, k_a_nop_g);
		gstat_t_l2_p = new Stat(GSTAT_T_L2_P, p_pa, k_t_l2_com_e, k_a_nop_g);
		gstat_t_l2_n = new Stat(GSTAT_T_L2_N, p_pa, k_t_l2_n_e, k_a_nop_g);
		gstat_t_l2_m = new Stat(GSTAT_T_L2_M, p_pa, k_t_l2_com_e, k_a_nop_g);
		gstat_t_c2_seg = new Stat(GSTAT_T_C2_SEG, p_pa, k_t_c2_seg_e, k_a_nop_g);
		gstat_t_c2_gra = new Stat(GSTAT_T_C2_GRA, p_pa, k_t_c2_gra_e, k_a_nop_g);
		gstat_t_c2_sou = new Stat(GSTAT_T_C2_SOU, p_pa, k_t_c2_sou_e, k_t_c2_sou_g);
		gstat_t_c2_ctr = new Stat(GSTAT_T_C2_CTR, p_pa, k_t_c2_ctr_e, k_a_nop_g);
		gstat_t_i2_abo = new Stat(GSTAT_T_I2_ABO, p_pa, k_t_i2_abo_e, k_a_nop_g);
		gstat_t_i2_sys = new Stat(GSTAT_T_I2_SYS, p_pa, k_t_i2_sys_e, k_a_nop_g);
		gstat_t_i2_lic = new Stat(GSTAT_T_I2_LIC, p_pa, k_t_i2_lic_e, k_a_nop_g);
		gstat_t_i2_sta = new Stat(GSTAT_T_I2_STA, p_pa, k_t_i2_sta_e, k_a_nop_g);
		
		g_stat = gstat_p_n;
		p_stat = gstat_p_n;
		
		barColor();
		
		initSound();
		
		if(!glfwInit()) {
			throw new RuntimeException("Failed to Initialize GLFW\n");
		}
		glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
		window = glfwCreateWindow(wsizex[scsw[0]], wsizey[scsw[0]], title, NULL, NULL);
		if(window == NULL) {
			throw new RuntimeException("Cannot create window\n");
		}
		
		try(var stack = stackPush()) {
			var pChannels = stack.ints(4);
			var pSize16 = stack.ints(16);
			var pSize32 = stack.ints(32);
			var pSize48 = stack.ints(48);
//			String fn16 = Paths.get(new URI(getSystemClassLoader()
//					.getResource("home/vcausality/launch/icon_cell16.png")
//					.toExternalForm()))
//					.toString();
			String fn16 = "assets/icon_cell16.png";
//			System.out.println(fn16);
//			String fn32 = Paths.get(new URI(getSystemClassLoader()
//					.getResource("home/vcausality/launch/icon_cell32.png")
//					.toExternalForm()))
//					.toString();
			String fn32 = "assets/icon_cell32.png";
//			String fn48 = Paths.get(new URI(getSystemClassLoader()
//					.getResource("home/vcausality/launch/icon_cell48.png")
//					.toExternalForm()))
//					.toString();
			String fn48 = "assets/icon_cell48.png";
			icon16 = stbi_load(fn16, pSize16, pSize16, pChannels, STBI_rgb_alpha);
			if(icon16 == null) {
				throw new RuntimeException("Failed to load icon16 image\n");
			}
			icon32 = stbi_load(fn32, pSize32, pSize32, pChannels, STBI_rgb_alpha);
			if(icon32 == null) {
				throw new RuntimeException("Failed to load icon32 image\n");
			}
			icon48 = stbi_load(fn48, pSize48, pSize48, pChannels, STBI_rgb_alpha);
			if(icon48 == null) {
				throw new RuntimeException("Failed to load icon48 image\n");
			}
			
			icon = GLFWImage.calloc(3, stack);
			icon.get(0).width(16).height(16).pixels(icon16);
			icon.get(1).width(32).height(32).pixels(icon32);
			icon.get(2).width(48).height(48).pixels(icon48);
			
			glfwSetWindowIcon(window, icon);
			
			stbi_image_free(icon16);
			stbi_image_free(icon32);
			stbi_image_free(icon48);
		}
		// catch(URISyntaxException e) {
		//	e.printStackTrace();
		//}
		
		glfwSetKeyCallback(window, (w, k, c, a, m) -> {
			if(a == GLFW_PRESS) {
				for(int i = 0; i < kcde.length; i++) {
					if(k == kcde[i]) {
						kflg[i] = 1;
						kcle[(i << 1) | 1] = 1;
					}
				}
				if(!actRequested) {
					g_stat.onKeyPressed.accept(k);
				}
				if(k == kcde[KEY_SCR]) {
					requestScreenShot();
				} else if(k == kcde[KEY_LOG]) {
					logRequested = true;
				}
			} else if(a == GLFW_RELEASE) {
				for(int i = 0; i < kcde.length; i++) {
					if(k == kcde[i]) {
						kflg[i] = 0;
						kcle[(i << 1) | 1] = 0;
					}
				}
			}
			
		});
		
		createShaderResources();
		
		return window;
	}
	
	static void mainLoop() {
		long t = System.currentTimeMillis();
		diff = t - prevTime;
		prevTime = t;
		if(!actRequested) {
			g_stat.onKeyPressing.run();
		}
		if(kflg[KEY_ACT] == 1) {
			actRequested = true;
		} else {
			actRequested = false;
		}
		g_stat.proc.run();
		
		if(drawFlg) {
			dr_ctrl();
		}
		
		if(logRequested) {
			logRequested = false;
			writeLog();
		}
	}
	
	static void afterDraw() {
		if(drawFlg) {
			clr_ctrl();
		} else {
			drawFlg = true;
		}
	}
	
	private static void initGame() {
		for(int i = 0; i < cells.length; i++) {cells[i] = C_VA;}
		for(int i = 0; i < ccnt.length; i++) {ccnt[i] = 0;}
		c_sw = C_VA;
		c_x = 0;
		c_y = 0;
		dn_cnt = 0;
		dn_max = 1;
		s_cnt = 0;
		l_cnt = 0;
		l_loc = 0;
		score = 0;
		hp = PLAYER_HP_X;
		qg_isVacant = true;
		barColor();
	}
	
	private static void barColor() {
		if(sgsw[BAR_HP] == 2) {
			int j = (hp % 2000) * 16 / 125;
			switch(hp / 2000) {
			case 5:
				i_bar_color = 0x0000FFFF;
				break;
			case 4:
				i_bar_color = 0x0000FFFF | ((255 - j) << 16);
				break;
			case 3:
				i_bar_color = 0x00FF00FF | (j << 16);
				break;
			case 2:
				i_bar_color = 0x00FF00FF | ((255 - j) << 24);
				break;
			case 1:
				i_bar_color = 0xFF0000FF | (j << 16);
				break;
			case 0:
				i_bar_color = j << 24;
				break;
			}
		} else if(sgsw[BAR_HP] == 1) {
			int j = hp / 100;
			i_bar_color = (j << 24) | (j << 16) | (j << 8) | 0x000000FF;
		} else {
			i_bar_color = 0;
		}
	}
	
	private static int selColor() {
		int i = (int)(Math.random() * 6);
		int j = (int)(Math.random() * 256);
		int k = 0;
		switch(i) {
		case 0:
			k = 0xFF000000 | (j << 16);
			break;
		case 1:
			k = (j << 24) | 0x00FF0000;
			break;
		case 2:
			k = 0x00FF0000 | (j << 8);
			break;
		case 3:
			k = (j << 16) | 0x0000FF00;
			break;
		case 4:
			k = (j << 24) | 0x0000FF00;
			break;
		case 5:
			k = 0xFF000000 | (j << 8);
			break;
		}
		return k;
	}
	
	private static int cntColor() {
		int c = 0;
		color_i++;
		if(color_i == 256) {
			color_i = 0;
			color_cnt++;
			if(color_cnt == 6) {
				color_cnt = 0;
			}
		}
		switch(color_cnt) {
		case 0:
			c = 0xFF0000FF | (color_i << 16);
			break;
		case 1:
			c = ((255 - color_i) << 24) | 0x00FF00FF;
			break;
		case 2:
			c = 0x00FF00FF | (color_i << 8);
			break;
		case 3:
			c = ((255 - color_i) << 16) | 0x0000FFFF;
			break;
		case 4:
			c = (color_i << 24) | 0x0000FFFF;
			break;
		case 5:
			c = 0xFF0000FF | ((255 - color_i) << 8);
			break;
		}
		return c;
	}
	
	private static int cntMonoc() {
		int c = 0;
		if(monoc_cnt == 0) {
			monoc_i++;
			if(monoc_i >= 255) {
				monoc_cnt = 1;
			}
		} else {
			monoc_i--;
			if(monoc_i <= 0) {
				monoc_cnt = 0;
			}
		}
		c = (monoc_i << 24) | (monoc_i << 16) | (monoc_i << 8) | 0x000000FF;
		return c;
	}
	
	private static void rot_r(boolean _t) {
		c_sw = cells[0];
		cells[0] = cells[12];
		cells[12] = cells[15];
		cells[15] = cells[3];
		cells[3] = c_sw;
		c_sw = cells[1];
		cells[1] = cells[8];
		cells[8] = cells[14];
		cells[14] = cells[7];
		cells[7] = c_sw;
		c_sw = cells[2];
		cells[2] = cells[4];
		cells[4] = cells[13];
		cells[13] = cells[11];
		cells[11] = c_sw;
		c_sw = cells[5];
		cells[5] = cells[9];
		cells[9] = cells[10];
		cells[10] = cells[6];
		cells[6] = c_sw;
		boolean res = true;
		if(_t) {
			res = req_mr();
			if(res && _t) {
				play(SE_CTRL);
			}
		}
		if(res) {
			check();
			add_wait(G_DP_ROT);
		} else {
			play(SE_NUL);
			rot_l(false);
		}
	}
	
	private static void rot_l(boolean _t) {
		c_sw = cells[0];
		cells[0] = cells[3];
		cells[3] = cells[15];
		cells[15] = cells[12];
		cells[12] = c_sw;
		c_sw = cells[1];
		cells[1] = cells[7];
		cells[7] = cells[14];
		cells[14] = cells[8];
		cells[8] = c_sw;
		c_sw = cells[2];
		cells[2] = cells[11];
		cells[11] = cells[13];
		cells[13] = cells[4];
		cells[4] = c_sw;
		c_sw = cells[5];
		cells[5] = cells[6];
		cells[6] = cells[10];
		cells[10] = cells[9];
		cells[9] = c_sw;
		boolean res = true;
		if(_t) {
			res = req_mr();
			if(res && _t) {
				play(SE_CTRL);
			}
		}
		if(res) {
			check();
			add_wait(G_DP_ROT);
		} else {
			play(SE_NUL);
			rot_r(false);
		}
	}
	
	private static void mov_r(boolean _t) {
		c_x++;
		boolean res = true;
		if(_t) {
			res = req_mr();
			if(res && _t) {
				play(SE_CV);
			}
		}
		if(res) {
			check();
			add_wait(G_DP_MOV);
		} else {
			play(SE_NUL);
			mov_l(false);
		}
	}
	
	private static void mov_l(boolean _t) {
		c_x--;
		boolean res = true;
		if(_t) {
			res = req_mr();
			if(res && _t) {
				play(SE_CV);
			}
		}
		if(res) {
			check();
			add_wait(G_DP_MOV);
		} else {
			play(SE_NUL);
			mov_r(false);
		}
	}
	
	private static void mov_dn() {
		if(g_stat.code == GSTAT_G_F) {
			if(dn_max == G_DN_DRP) {
				dn_max = ldnc[g_level];
				dn_cnt = ldnc[g_level];
			}
		} else {
			c_y++;
			boolean res = req_mr();
			if(res) {
				dn_cnt = dn_max;
			} else {
				c_y--;
				g_stat = gstat_g_f;
				dn_cnt = ldnc[g_level];
				dn_max = ldnc[g_level];
			}
		}
	}
	
	private static void push() {
		if(s_cnt < 3) {
			int i = 0;
			switch(s_cnt) {
			case 0:
				i = STACK_2;
				break;
			case 1:
				i = STACK_1;
				break;
			case 2:
				i = STACK_0;
				break;
			}
			boolean exp = false;
			for(int j = 0; j < 16; j++) {
				cells[i] = cells[j];
				if((cells[i] & 0x000000FF) == C_T2) {
					cells[i] = (cells[i] & 0xFFFFFF00) | C_TG;
				} else if(cells[i] == C_BM) {
					exp = true;
					hp -= D_BM;
				}
				i++;
			}
			play(SE_STACK);
			if(exp) {
				for(int j = i - 16; j < i; j++) {
					if(cells[j] != C_VA) {
						cells[j] = C_BF;
					}
				}
			}
			s_cnt++;
			if(hp > 0) {
				g_stat = gstat_g_s;
			} else {
				play(SE_GMOVR);
				hp = 0;
				g_stat = gstat_p_d;
			}
			barColor();
		} else {
			int i = STACK_0;
			for(int j = 0; j < 16; j++) {
				c_sw = cells[j];
				cells[j] = cells[i];
				cells[i] = c_sw;
				i++;
			}
			boolean res = req_mr();
			if(res) {
				boolean exp = false;
				i = STACK_0;
				for(int j = 0; j < 16; j++) {
					if((cells[i] & 0x000000FF) == C_T2) {
						cells[i] = (cells[i] & 0xFFFFFF00) | C_TG;
					} else if(cells[i] == C_BM) {
						exp = true;
						hp -= D_BM;
					}
					i++;
				}
				play(SE_STACK);
				if(exp) {
					for(int j = i - 16; j < i; j++) {
						if(cells[j] != C_VA) {
							cells[j] = C_BF;
						}
					}
				}
				if(hp <= 0) {
					play(SE_GMOVR);
					hp = 0;
					g_stat = gstat_p_d;
				}
				check();
				barColor();
			} else {
				play(SE_NUL);
				i = STACK_0;
				for(int j = 0; j < 16; j++) {
					c_sw = cells[i];
					cells[i] = cells[j];
					cells[j] = c_sw;
					i++;
				}
			}
		}
	}
	
	private static void pop() {
		if(s_cnt > 0) {
			if(qg_isVacant) {
				int i = QUEUE_G;
				int j = 0;
				switch(s_cnt) {
				case 1:
					j = STACK_2;
					break;
				case 2:
					j = STACK_1;
					break;
				case 3:
					j = STACK_0;
					break;
				}
				for(int k = 0; k < 16; k++) {
					cells[i] = cells[k];
					cells[k] = cells[j];
					i++;
					j++;
				}
				boolean res = req_mr();
				if(res) {
					for(int k = j - 16; k < j; k++) {
						cells[k] = C_VA;
					}
					qg_isVacant = false;
					s_cnt -= 1;
					check();
					play(SE_STACK2);
				} else {
					play(SE_NUL);
					i -= 16;
					for(int k = 0; k < 16; k++) {
						cells[k] = cells[i];
						cells[i] = C_VA;
						i++;
					}
				}
			} else {
				int i = 0;
				switch(s_cnt) {
				case 1:
					i = STACK_2;
					break;
				case 2:
					i = STACK_1;
					break;
				case 3:
					i = STACK_0;
					break;
				}
				for(int j = 0; j < 16; j++) {
					c_sw = cells[j];
					cells[j] = cells[i];
					cells[i] = c_sw;
					i++;
				}
				boolean res = req_mr();
				if(res) {
					boolean exp = false;
					for(int j = i - 16; j < i; j++) {
						if((cells[j] & 0x000000FF) == C_T2) {
							cells[j] = (cells[j] & 0xFFFFFF00) | C_TG;
						} else if(cells[j] == C_BM) {
							exp = true;
							hp -= D_BM;
						}
					}
					play(SE_STACK2);
					if(exp) {
						for(int j = i - 16; j < i; j++) {
							if(cells[j] != C_VA) {
								cells[j] = C_BF;
							}
						}
					}
					if(hp <= 0) {
						play(SE_GMOVR);
						hp = 0;
						g_stat = gstat_p_d;
					}
					check();
					barColor();
				} else {
					play(SE_NUL);
					i -= 16;
					for(int j = 0; j < 16; j++) {
						c_sw = cells[j];
						cells[j] = cells[i];
						cells[i] = c_sw;
						i++;
					}
				}
			}
		} else {
			play(SE_NUL);
		}
	}
	
	private static boolean req_mr() {
		int x;
		int y = c_y;
		int cnt = 0;
		boolean ret = true;
		for(int i = 0; i < 4; i++) {
			x = c_x;
			if(y >= 0 && y < 32) {
				for(int j = 0; j < 4; j++) {
					if(x >= 0 && x < 14) {
						if(cells[cnt] != C_VA && cells[BOARD_H + y * OPN_W + x] != C_VA) {
							ret = false;
						}
					} else {
						if(cells[cnt] != C_VA) {
							ret = false;
						}
					}
					cnt++;
					x++;
				}
			} else {
				for(int j = 0; j < 4; j++) {
					if(cells[cnt] != C_VA) {
						ret = false;
					}
					cnt++;
				}
				x += 4;
			}
			y++;
		}
		return ret;
	}
	
	private static void check() {
		c_y++;
		if(req_mr()) {
			if(g_stat.code == GSTAT_G_F) {
				dn_cnt = ldnc[g_level];
				dn_max = ldnc[g_level];
				g_stat = gstat_g_n;
			}
		} else {
			dn_cnt = ldnc[g_level];
			dn_max = ldnc[g_level];
			g_stat = gstat_g_f;
		}
		c_y--;
	}
	
	private static int[] judge() {
		int[] ret = new int[5];
		int i = BOARD_H + c_y * OPN_W;
		int j = 0;
		boolean flg;
		for(int k = 0; k < 4; k++) {
			flg = true;
			if(c_y + k < 32) {
				for(int l = 0; l < OPN_W; l++) {
					if(cells[i] == C_VA) {
						flg = false;
					}
					if(cells[i] == C_BM) {
						cells[i] = C_BG;
					}
					if((cells[i] & 0x000000FF) == C_T2) {
						cells[i] = C_T2;
					}
					i++;
				}
			} else {
				flg = false;
			}
			if(flg) {
				ret[k] = 1;
				l_cnt++;
				l_loc++;
				j++;
			}
		}
		ret[4] = j;
		return ret;
	}
	
	private static void del_drop(int[] res) {
		int[] ib = new int[4];
		for(int i = 0; i < 4; i++) {
			ib[i] = res[i];
		}
		int[] ia = new int[4];
		int p = 0;
		for(int i = c_y; i < c_y + 4; i++) {
			if(i >= 32) {
				p++;
			}
		}
		int j = 3 - p;
		int k = 3 - p;
		int q = 0;
		for(int i = 0; i < 4 - p; i++) {
			if(ib[j] == 0) {
				ia[k] = j;
				if(j != k) {
					ib[j] = 1;
				}
				k--;
			} else {
				q++;
			}
			j--;
		}
		int l = BOARD_H + (c_y + 4) * OPN_W - 1;
		j = 3 - p;
		for(int i = 0; i < 4 - q; i++) {
			if(c_y + 3 - i < 32) {
				int m = BOARD_H + (c_y + ia[j] + 1) * OPN_W - 1;
				for(int n = 0; n < OPN_W; n++) {
					cells[l] = cells[m];
					l--;
					m--;
				}
				j--;
			} else {
				l -= OPN_W;
			}
		}
		j = BOARD_H + c_y * OPN_W - 1;
		k = BOARD_H + (c_y + q) * OPN_W - 1;
		if(j != k) {
			for(int i = 0; i < c_y * OPN_W; i++) {
				cells[k] = cells[j];
				cells[j] = C_VA;
				j--;
				k--;
			}
		}
		for(int i = BOARD_H; i < OPN_W * 4 + BOARD_H; i++) {
			if(cells[i] != C_VA) {
				hzd = true;
			}
		}
	}
	
	private static void calc_score(int[] res) {
		int i = c_y * OPN_W + BOARD_H;
		int j = res[4] - 1;
		for(int k = 0; k < 4; k++) {
			if(res[k] == 1) {
				for(int l = 0; l < OPN_W; l++) {
					score += rate[cells[i] & 0x000000FF] * comp[j];
					hp += heal[cells[i] & 0x000000FF];
					ccnt[cells[i] & 0x000000FF]++;
					i++;
				}
			} else {
				i += OPN_W;
			}
		}
		if(hp > PLAYER_HP_X) {
			hp = PLAYER_HP_X;
		}
		barColor();
	}
	
	private static void calc_dmg() {
		for(int i = BOARD_H; i < BOARD_H + COL_H; i++) {
			if(cells[i] != C_VA) {
				hp -= cdmg[cells[i] & 0x000000FF];
				cells[i] = C_VA;
			}
		}
		for(int i = BOARD_V; i < BOARD_V + COL_V; i++) {
			hp -= cdmg[cells[i] & 0x000000FF];
			cells[i] = C_VA;
		}
		if(hp <= 0) {
			play(SE_GMOVR);
			hp = 0;
			g_stat = gstat_p_d;
		}
		barColor();
	}
	
	private static void dr_ctrl() {
		int k = 0;
		for(int i = c_y; i < c_y + 4; i++) {
			if(i < 32) {
				for(int j = c_x; j < c_x + 4; j++) {
					if(j >= 0 && j < OPN_W) {
						if(cells[k] != C_VA) {
							cells[BOARD_H + i * OPN_W + j] = cells[k];
						}
					}
					k++;
				}
			}
		}
	}
	
	private static void clr_ctrl() {
		int k = 0;
		for(int i = c_y; i < c_y + 4; i++) {
			if(i < 32) {
				for(int j = c_x; j < c_x + 4; j++) {
					if(j >= 0 && j < OPN_W) {
						if(cells[k] != C_VA) {
							cells[BOARD_H + i * OPN_W + j] = C_VA;
						}
					}
					k++;
				}
			}
		}
	}
	
	private static void add_wait(int t) {
		dn_cnt += t;
		if(dn_cnt > dn_max) {
			dn_cnt = dn_max;
		}
	}
	
	private static int gen_c1() {
		int i = (int)(Math.random() * 65536);
		int j = (int)(Math.random() * 16);
		int k = 0;
		int m = 0;
		for(int l = 1; l < (1 << 16); l <<= 1) {
			if((i & l) != 0) {
				k++;
			}
		}
		if(k < 8) {
			i |= 1632;
		}
		if(j < 7) {
			m = (int)(Math.random() * 256);
			if(m < 16) {
				i = 65535;
			} else if(m < 40) {
				i = 864;
			} else if(m < 64) {
				i = 3168;
			} else if(m < 96) {
				i = 8800;
			} else if(m < 128) {
				i = 17504;
			} else if(m < 168) {
				i = 1824;
			} else if(m < 208) {
				i = 1632;
			} else {
				i = 8738;
			}
		} else if(j < 10) {
			m = (int)(Math.random() * 256);
			if(m < 24) {
				i = 26214;
			} else if(m < 48) {
				i = 1911;
			} else if(m < 88) {
				i = 8736;
			} else {
				i = 608;
			}
		} else if(j < 15) {
			m = (int)(Math.random() * 4);
			if(m == 0) {
				i &= 61152;
			} else if(m == 1) {
				i &= 30576;
			} else if(m == 2) {
				i &= 3822;
			} else if(m == 3) {
				i &= 1911;
			}
		} else {
			i |= 28862;
		}
		return i;
	}
	
	private static void gen_c2(int vo, int offset) {
		int i = (int)(Math.random() * 256);
		int j;
		if(i < 128) {
			j = C_BM;
		} else {
			j = C_T2;
		}
		for(int k = 0; k < 16; k++) {
			if((vo & (1 << k)) != 0) {
				int l = (int)(Math.random() * 256);
				if(l < ltoc[g_level]) {
					cells[offset + k] = C_OC;
				} else if(l < ltt1[g_level]) {
					cells[offset + k] = selColor() | C_T1;
				} else {
					cells[offset + k] = (j == C_T2 ? selColor() : 0) | j;
				}
			} else {
				cells[offset + k] = C_VA;
			}
		}
	}
	
	private static void gen_i() {
		gen_c2(gen_c1(), QUEUE_0);
		gen_c2(gen_c1(), QUEUE_1);
		gen_c2(gen_c1(), QUEUE_2);
		gen_c2(gen_c1(), QUEUE_3);
		gen_c2(gen_c1(), QUEUE_4);
	}
	
	private static void dept() {
		c_x = (int)(Math.random() * 11);
		c_y = 0;
		for(int i = 0; i < 16; i++) {
			cells[CONTROL + i] = cells[QUEUE_0 + i];
			cells[QUEUE_0 + i] = cells[QUEUE_1 + i];
			cells[QUEUE_1 + i] = cells[QUEUE_2 + i];
			cells[QUEUE_2 + i] = cells[QUEUE_3 + i];
			cells[QUEUE_3 + i] = cells[QUEUE_4 + i];
		}
		if(qg_isVacant) {
			gen_c2(gen_c1(), QUEUE_4);
		} else {
			for(int i = 0; i < 16; i++) {
				cells[QUEUE_4 + i] = cells[QUEUE_G + i];
				cells[QUEUE_G + i] = C_VA;
			}
			qg_isVacant = true;
		}
	}
	
	static void draw(VkCommandBuffer c, int i) {
		background(c, i, SCREEN);
		drawingFrameBarrier(c, i);
		
		for(var n : PANELS) {
			copy(c, i, n);
		}
		drawingFrameBarrier(c, i);
			
//		ascii(c, i, 100, 10, String.format("%08X", g_stat.code));
//		drawingFrameBarrier(c, i);
		
		if(g_stat.code > 0 || kflg[KEY_ACT] == 1) {
			for(var n : SHADOWS) {
				rect(c, i, n);
			}
			drawingFrameBarrier(c, i);
			cells(c, i);
			if(p_stat.code != GSTAT_P_N) {
				LOCAL.srcCoord(651, g_mode * 24).dstCoord(535, 340).cpyRegion(167, 24).copy(c, i);
				LOCAL.srcCoord(420, 901).dstCoord(500, 829).cpyRegion(100, 50).copy(c, i);
				LOCAL.srcCoord(420, g_stat.code == GSTAT_G_F ? 801 : 851).dstCoord(54, 50).copy(c, i);
			}
		} else {
			copy(c, i, MENU);
		}
		drawingFrameBarrier(c, i);
			
		control(c, i);
		guide(c, i);
		segment(c, i);
		int d;
		switch(sgsw[SEGMENT_ICNT_IDX]) {
		case 2:
			d = cntColor();
			BG_CNT_T1.color(d);
			BG_CNT_TG.color(d);
			break;
		case 1:
			d = cntMonoc();
			BG_CNT_T1.color(d);
			BG_CNT_TG.color(d);
			break;
		case 0:
			BG_CNT_T1.color(0xCFCFCFFF);
			BG_CNT_TG.color(0xCFCFCFFF);
			break;
		}
		copy(c, i, BG_CNT_T1);
		copy(c, i, BG_CNT_TG);
		copy(c, i, BG_HP_BAR);
		
		if(g_stat.code <= 0 && kflg[KEY_ACT] != 1) {
			if(g_stat.code == GSTAT_P_N) {
				LOCAL.srcCoord(24, 825).dstCoord(236, 213).cpyRegion(176, 80).copy(c, i);
				LOCAL.srcCoord(0, 712).dstCoord(164, cur_v * 24 + 305).cpyRegion(16, 16).copy(c, i);
				ascii(c, i, 188, 303, "PLAY");
				ascii(c, i, 188, 327, "CONFIG");
				ascii(c, i, 188, 351, "INFO");
				ascii(c, i, 188, 375, "QUIT");
				ascii(c, i, 164, 853,
						"["+ gcde[KEY_CVU]+"]["+ gcde[KEY_CVD] +"] Select ["+ gcde[KEY_DEC] +"] Decide");
			} else if(g_stat.code == GSTAT_P_P) {
				LOCAL.srcCoord(651, 144).dstCoord(162, 243).cpyRegion(244, 52).copy(c, i);
				LOCAL.srcCoord(0, 712).dstCoord(164, cur_v * 24 + 305).cpyRegion(16, 16).copy(c, i);
				ascii(c, i, 188, 303, "RESUME");
				ascii(c, i, 188, 327, "CONFIG");
				ascii(c, i, 188, 351, "TITLE");
				ascii(c, i, 188, 375, "INFO");
				ascii(c, i, 188, 399, "QUIT");
				ascii(c, i, 164, 829, "["+ gcde[KEY_ACT] +"] Look in the board");
				ascii(c, i, 164, 853,
						"["+ gcde[KEY_CVU]+"]["+ gcde[KEY_CVD] +"] Select ["+ gcde[KEY_DEC] +"] Decide");
			} else if(g_stat.code == GSTAT_P_S) {
				LOCAL.srcCoord(651, 196).dstCoord(162, 243).cpyRegion(244, 52).copy(c, i);
				LOCAL.srcCoord(0, 712).dstCoord(164, cur_v * 24 + 305).cpyRegion(16, 16).copy(c, i);
				ascii(c, i, 188, 303, "STAFF");
				ascii(c, i, 188, 327, "TITLE");
				ascii(c, i, 188, 351, "QUIT");
				ascii(c, i, 164, 829, "["+ gcde[KEY_ACT] +"] Look in the board");
				ascii(c, i, 164, 853,
						"["+ gcde[KEY_CVU]+"]["+ gcde[KEY_CVD] +"] Select ["+ gcde[KEY_DEC] +"] Decide");
			} else if(g_stat.code == GSTAT_P_D) {
				LOCAL.srcCoord(651, 248).dstCoord(162, 243).cpyRegion(244, 52).copy(c, i);
				LOCAL.srcCoord(0, 712).dstCoord(164, cur_v * 24 + 305).cpyRegion(16, 16).copy(c, i);
				ascii(c, i, 188, 303, "TITLE");
				ascii(c, i, 188, 327, "QUIT");
				ascii(c, i, 164, 829, "["+ gcde[KEY_ACT] +"] Look in the board");
				ascii(c, i, 164, 853,
						"["+ gcde[KEY_CVU]+"]["+ gcde[KEY_CVD] +"] Select ["+ gcde[KEY_DEC] +"] Decide");
				
			} else if(g_stat.code == GSTAT_T_L1 || ((g_stat.code & GSTAT_T_L1_N) == GSTAT_T_L1_N)) {
				int ii = cur_v * 24 + 305;
				LOCAL.srcCoord(0, 712).dstCoord(164, ii).cpyRegion(16, 16).copy(c, i);
				if((g_stat.code & GSTAT_T_L1_N) == GSTAT_T_L1_N) {
					LOCAL.srcCoord(0, 808).dstCoord(260, ii).copy(c, i);
					LOCAL.srcCoord(0, 792).dstCoord(452, ii).copy(c, i);
				}
				switch(cur_v) {
				case 0:
					ascii(c, i, 164, 543, "6 levels (1~6)");
					ascii(c, i, 164, 563, "A little profit and low speed");
					break;
				case 1:
					ascii(c, i, 164, 543, "7 levels (4~10)");
					ascii(c, i, 164, 563, "Standard setting");
					break;
				case 2:
					ascii(c, i, 164, 543, "8 levels (7~14)");
					ascii(c, i, 164, 563, "Much profit and high speed");
					break;
				case 3:
					ascii(c, i, 164, 543, "9 levels (10~18)");
					ascii(c, i, 164, 563, "Great wealth is staked on your control.");
					break;
				case 4:
					ascii(c, i, 164, 543, "Endless mode, selected level (1~20)");
					break;
				case 5:
					ascii(c, i, 164, 543, "Intended impossibility");
					break;
				}
				LOCAL.srcCoord(651, 404).dstCoord(164, 243).cpyRegion(244, 52).copy(c, i);
				ascii(c, i, 188, 303, "COPPER");
				ascii(c, i, 188, 327, "SILVER");
				ascii(c, i, 188, 351, "GOLD");
				ascii(c, i, 188, 375, "PLATINUM");
				ascii(c, i, 188, 399, "IRON");
				if(ir_open) {
					ascii(c, i, 188, 423, "IRIDIUM");
				}
				switch(g_stat.code) {
				case GSTAT_T_L2_C:
					ascii(c, i, 292, 303, "01-06");
					LOCAL.srcCoord(0, 664).dstCoord(372, 305).cpyRegion(16, 16).copy(c, i);
					LOCAL.srcCoord(0, 648).dstCoord(388, 305).copy(c, i);
					LOCAL.dstCoord(404, 305).copy(c, i);
					LOCAL.dstCoord(420, 305).copy(c, i);
					break;
				case GSTAT_T_L2_S:
					ascii(c, i, 292, 327, "04-10");
					LOCAL.srcCoord(0, 664).dstCoord(372, 329).cpyRegion(16, 16).copy(c, i);
					LOCAL.dstCoord(388, 329).copy(c, i);
					LOCAL.srcCoord(0, 648).dstCoord(404, 329).copy(c, i);
					LOCAL.dstCoord(420, 329).copy(c, i);
					break;
				case GSTAT_T_L2_G:
					ascii(c, i, 292, 351, "07-14");
					LOCAL.srcCoord(0, 664).dstCoord(372, 353).cpyRegion(16, 16).copy(c, i);
					LOCAL.dstCoord(388, 353).copy(c, i);
					LOCAL.dstCoord(404, 353).copy(c, i);
					LOCAL.srcCoord(0, 648).dstCoord(420, 353).copy(c, i);
					break;
				case GSTAT_T_L2_P:
					ascii(c, i, 292, 375, "10-18");
					LOCAL.srcCoord(0, 664).dstCoord(372, 377).cpyRegion(16, 16).copy(c, i);
					LOCAL.dstCoord(388, 377).copy(c, i);
					LOCAL.dstCoord(404, 377).copy(c, i);
					LOCAL.dstCoord(420, 377).copy(c, i);
					break;
				case GSTAT_T_L2_N:
					LOCAL.srcCoord(0, 680).dstCoord(276, 401).cpyRegion(16, 16).copy(c, i);
					LOCAL.srcCoord(0, 696).dstCoord(356, 401).copy(c, i);
					LOCAL.srcCoord(0, kflg[KEY_CVL] == 1 ? 744 : 728).dstCoord(292, 401).copy(c, i);
					LOCAL.srcCoord(0, kflg[KEY_CVR] == 1 ? 776 : 760).dstCoord(340, 401).copy(c, i);
					if(cur_h == 18) {
						ascii(c, i, 372, 399, "Training??");
						ascii(c, i, 164, 563, "It looks like a spoil tip...");
					} else if(cur_h == 19) {
						ascii(c, i, 372, 399, "Training??");
						ascii(c, i, 164, 563, "Efficient business");
					}
					ascii(c, i, 316, 399, String.format("%02d", cur_h + 1));
					break;
				case GSTAT_T_L2_M:
					ascii(c, i, 316, 423, "21");
					break;
				}
				if(g_stat.code == GSTAT_T_L1) {
					ascii(c, i, 164, 853, "["+ gcde[KEY_CXL] +"] Back ["+ gcde[KEY_DEC] +"] Decide level");
				} else {
					ascii(c, i, 164, 853, "["+ gcde[KEY_CXL] +"] Back ["+ gcde[KEY_DEC] +"] Start");
				}
			} else if(g_stat.code == GSTAT_T_C1) {
				LOCAL.srcCoord(651, 456).dstCoord(164, 243).cpyRegion(244, 52).copy(c, i);
				LOCAL.srcCoord(0, 712).dstCoord(164, cur_v * 24 + 305).cpyRegion(16, 16).copy(c, i);
				ascii(c, i, 188, 303, "SEGMENT");
				ascii(c, i, 188, 327, "GRAPHICS");
				ascii(c, i, 188, 351, "SOUND");
				ascii(c, i, 188, 375, "CONTROL");
				ascii(c, i, 164, 853, "["+ gcde[KEY_CXL] +"] Back");
			} else if(g_stat.code == GSTAT_T_I1) {
				LOCAL.srcCoord(651, 508).dstCoord(164, 243).cpyRegion(244, 52).copy(c, i);
				LOCAL.srcCoord(0, 712).dstCoord(164, cur_v * 24 + 305).cpyRegion(16, 16).copy(c, i);
				ascii(c, i, 188, 303, "ABOUT");
				ascii(c, i, 188, 327, "SYSTEM");
				ascii(c, i, 188, 351, "LICENSE");
				ascii(c, i, 188, 375, "STAFF");
				ascii(c, i, 164, 853, "["+ gcde[KEY_CXL] +"] Back");
			} else if(g_stat.code == GSTAT_T_C2_SEG) {
				LOCAL.srcCoord(0, 712).dstCoord(164, cur_v * 24 + 305).cpyRegion(16, 16).copy(c, i);
				for(int j = 0; j < sgsw.length; j++) {
					int k = j * 24 + 305;
					int l = 340;
					if(sglm[j] == 2) {
						l = sgsw[j] == 1 ? 292 : 356;
					} else {
						switch(sgsw[j]) {
						case 1:
							l = 388;
							break;
						case 2:
							l = 292;
							break;
						}
					}
					if(cfg_sel && j == cur_v){
						LOCAL.srcCoord(0, 824).dstCoord(l, k).copy(c, i);
						LOCAL.srcCoord(0, 840).dstCoord(l + 16, k).copy(c, i);
						if(sglm[j] == 2) {
							LOCAL.dstCoord(l + 32, k).copy(c, i);
							LOCAL.srcCoord(0, 856).dstCoord(l + 48, k).copy(c, i);
						} else {
							LOCAL.srcCoord(0, 856).dstCoord(l + 32, k).copy(c, i);
						}
						LOCAL.srcCoord(0, kflg[KEY_CVL] == 1 ? 744 : 728).dstCoord(276, k).copy(c, i);
						LOCAL.srcCoord(0, kflg[KEY_CVR] == 1 ? 776 : 760)
								.dstCoord(sglm[j] == 2 ? 420 : 436, k).copy(c, i);
					} else {
						LOCAL.srcCoord(0, 872).dstCoord(l, k).copy(c, i);
						LOCAL.srcCoord(0, 888).dstCoord(l + 16, k).copy(c, i);
						if(sglm[j] == 2) {
							LOCAL.dstCoord(l + 32, k).copy(c, i);
							LOCAL.srcCoord(0, 904).dstCoord(l + 48, k).copy(c, i);
						} else {
							LOCAL.srcCoord(0, 904).dstCoord(l + 32, k).copy(c, i);
						}
					}
				}
				drawingFrameBarrier(c, i);
				for(int j = 0; j < sgsw.length; j++) {
					int k = j * 24 + 303;
					if(sglm[j] == 2) {
						ascii(c, i, 316, k, "ON");
						ascii(c, i, 376, k, "OFF");
					} else {
						ascii(c, i, 300, k, "HIGH");
						ascii(c, i, 352, k, "OFF");
						ascii(c, i, 400, k, "LOW");
					}
				}
				LOCAL.srcCoord(651, 612).dstCoord(164, 243).cpyRegion(244, 52).copy(c, i);
				ascii(c, i, 188, 303, "CLOCK");
				ascii(c, i, 188, 327, "LEVEL");
				ascii(c, i, 188, 351, "WAIT");
				ascii(c, i, 188, 375, "HP");
				ascii(c, i, 188, 399, "SCORE");
				ascii(c, i, 188, 423, "LINES");
				ascii(c, i, 188, 447, "COUNT");
				ascii(c, i, 188, 471, "HP BAR");
				ascii(c, i, 188, 495, "COUNT ICON");
				if(cfg_sel) {
					ascii(c, i, 164, 829,
							"["+ gcde[KEY_CVL] +"]["+ gcde[KEY_CVR] +"] Select ["+ gcde[KEY_RST] +"] Default");
					ascii(c, i, 164, 853,
							"["+ gcde[KEY_CXL] +"] Discard + Back ["+ gcde[KEY_DEC] +"] Apply + Back");
				} else {
					ascii(c, i, 164, 853, "["+ gcde[KEY_CXL] +"] Back ["+ gcde[KEY_DEC] +"] Select");
				}
				if(sglm[cur_v] == 2) {
					ascii(c, i, 164, 543, "Turn ON/OFF a segment on the panel.");
				} else {
					ascii(c, i, 164, 543, "HIGH: Turn on and use a color scheme");
					ascii(c, i, 164, 567, "LOW : Turn on it monochromatic");
					ascii(c, i, 164, 591, "OFF : Turn off");
				}
			} else if(g_stat.code == GSTAT_T_C2_GRA) {
				LOCAL.srcCoord(0, 712).dstCoord(164, cur_v * 24 + 305).cpyRegion(16, 16).copy(c, i);
				for(int j = 0; j < scsw.length; j++) {
					int k = j * 24 + 305;
					int l = scsw[j] == 1 ? 292 : 356;
					if(cfg_sel && j == cur_v){
						LOCAL.srcCoord(0, 824).dstCoord(l, k).copy(c, i);
						LOCAL.srcCoord(0, 840).dstCoord(l + 16, k).copy(c, i);
						LOCAL.dstCoord(l + 32, k).copy(c, i);
						LOCAL.srcCoord(0, 856).dstCoord(l + 48, k).copy(c, i);
						LOCAL.srcCoord(0, kflg[KEY_CVL] == 1 ? 744 : 728).dstCoord(276, k).copy(c, i);
						LOCAL.srcCoord(0, kflg[KEY_CVR] == 1 ? 776 : 760).dstCoord(420, k).copy(c, i);
					} else {
						LOCAL.srcCoord(0, 872).dstCoord(l, k).copy(c, i);
						LOCAL.srcCoord(0, 888).dstCoord(l + 16, k).copy(c, i);
						LOCAL.dstCoord(l + 32, k).copy(c, i);
						LOCAL.srcCoord(0, 904).dstCoord(l + 48, k).copy(c, i);
					}
				}
				drawingFrameBarrier(c, i);
				for(int j = 0; j <= SCR_CSIZE; j++) {
					int k = j * 24 + 303;
					ascii(c, i, 316, k, "x2");
					ascii(c, i, 380, k, "x1");
				}
				for(int j = SCR_CSIZE + 1; j <= SCR_FPS; j++) {
					int k = j * 24 + 303;
					ascii(c, i, 316, k, "ON");
					ascii(c, i, 376, k, "OFF");
				}
				LOCAL.srcCoord(651, 300).dstCoord(164, 243).cpyRegion(244, 52).copy(c, i);
				ascii(c, i, 188, 303, "RESOLUTION");
				ascii(c, i, 188, 327, "V. SYNC.");
				if(cfg_sel) {
					ascii(c, i, 164, 829,
							"["+ gcde[KEY_CVL] +"]["+ gcde[KEY_CVR] +"] Select ["+ gcde[KEY_RST] +"] Default");
					ascii(c, i, 164, 853,
							"["+ gcde[KEY_CXL] +"] Discard + Back ["+ gcde[KEY_DEC] +"] Apply + Back");
				} else {
					ascii(c, i, 164, 853, "["+ gcde[KEY_CXL] +"] Back ["+ gcde[KEY_DEC] +"] Select");
				}
				ascii(c, i, 164, 543, "x1: (W) 800 x (H) 900");
				ascii(c, i, 164, 567, "x2: (W)1600 x (H)1800");
			} else if(g_stat.code == GSTAT_T_C2_SOU) {
				LOCAL.srcCoord(0, 712).dstCoord(164, cur_v * 24 + 305).cpyRegion(16, 16).copy(c, i);
				for(int j = 0; j < sosw.length; j++) {
					int k = j * 24 + 305;
					if(cfg_sel && j == cur_v){
						LOCAL.srcCoord(0, kflg[KEY_CVL] == 1 ? 744 : 728).dstCoord(292, k).copy(c, i);
						LOCAL.srcCoord(0, kflg[KEY_CVR] == 1 ? 776 : 760).dstCoord(340, k).copy(c, i);
						LOCAL.srcCoord(0, 808).dstCoord(260, k).copy(c, i);
						LOCAL.srcCoord(0, 792).dstCoord(452, k).copy(c, i);
					}
				}
				drawingFrameBarrier(c, i);
				for(int j = 0; j < sosw.length; j++) {
					int k = j * 24 + 303;
					ascii(c, i, 312, k, String.format("%3d", sosw[j]));
				}
				LOCAL.srcCoord(651, 352).dstCoord(164, 243).cpyRegion(244, 52).copy(c, i);
				ascii(c, i, 188, 303, "SE");
				if(cfg_sel) {
					ascii(c, i, 164, 829,
							"["+ gcde[KEY_CVL] +"]["+ gcde[KEY_CVR] +"] Select ["+ gcde[KEY_RST] +"] Default");
					ascii(c, i, 164, 853,
							"["+ gcde[KEY_CXL] +"] Discard + Back ["+ gcde[KEY_DEC] +"] Apply + Back");
				} else {
					ascii(c, i, 164, 853, "["+ gcde[KEY_CXL] +"] Back ["+ gcde[KEY_DEC] +"] Select");
				}
				ascii(c, i, 164, 543,
						"Volume (0~100) MIN ["+ gcde[KEY_CVL] +"] ["+ gcde[KEY_CVR] +"] MAX");
				ascii(c, i, 164, 567,
						"["+ gcde[KEY_NOP] +"]+["+ gcde[KEY_CVL]+"]["+ gcde[KEY_CVR] +"] Select finely");
			} else if(g_stat.code == GSTAT_T_C2_CTR) {
				LOCAL.srcCoord(0, 712).dstCoord(164, cur_v * 24 + 305).cpyRegion(16, 16).copy(c, i);
				for(int j = 0; j < ctsw.length; j++) {
					int k = j * 24 + 305;
					if(cfg_sel && j == cur_v){
						int l = cur_h == 1 ? 356 : 292;
						LOCAL.srcCoord(0, 824).dstCoord(l, k).copy(c, i);
						LOCAL.srcCoord(0, 840).dstCoord(l + 16, k).copy(c, i);
						LOCAL.dstCoord(l + 32, k).copy(c, i);
						LOCAL.srcCoord(0, 856).dstCoord(l + 48, k).copy(c, i);
						LOCAL.srcCoord(0, kflg[KEY_CVL] == 1 ? 744 : 728).dstCoord(276, k).copy(c, i);
						LOCAL.srcCoord(0, kflg[KEY_CVR] == 1 ? 776 : 760).dstCoord(420, k).copy(c, i);
					} else {
						int l = ctsw[j] == 1 ? 356 : 292;
						LOCAL.srcCoord(0, 872).dstCoord(l, k).copy(c, i);
						LOCAL.srcCoord(0, 888).dstCoord(l + 16, k).copy(c, i);
						LOCAL.dstCoord(l + 32, k).copy(c, i);
						LOCAL.srcCoord(0, 904).dstCoord(l + 48, k).copy(c, i);
					}
				}
				drawingFrameBarrier(c, i);
				for(int j = 0; j < ctsw.length; j++) {
					int k = j * 24 + 303;
					ascii(c, i, 318, k, "A");
					ascii(c, i, 382, k, "B");
				}
				LOCAL.srcCoord(651, 560).dstCoord(164, 243).cpyRegion(244, 52).copy(c, i);
				ascii(c, i, 188, 303, "TYPE");
				if(cfg_sel) {
					ascii(c, i, 164, 829,
							"["+ gcde[KEY_CVL] +"]["+ gcde[KEY_CVR] +"] Select ["+ gcde[KEY_RST] +"] Default");
					ascii(c, i, 164, 853,
							"["+ gcde[KEY_CXL] +"] Discard + Back ["+ gcde[KEY_DEC] +"] Apply + Back");
				} else {
					ascii(c, i, 164, 853, "["+ gcde[KEY_CXL] +"] Back ["+ gcde[KEY_DEC] +"] Select");
				}
				ascii(c, i, 164, 543, "Type A: Move with [A][D] keys");
				ascii(c, i, 164, 567, "  Playable on 60% keyboard");
				ascii(c, i, 164, 591, "Type B: Move with [<-][->] keys");
				ascii(c, i, 164, 615, "  Almost reversed the Type A");
				ascii(c, i, 164, 639, "Press a ["+ gcde[KEY_DEC] +"] to reflect.");
			} else if(g_stat.code == GSTAT_T_I2_ABO) {
				LOCAL.srcCoord(651, 664).dstCoord(164, 243).cpyRegion(244, 52).copy(c, i);
				switch(cur_h) {
				case 0:
					ascii(c, i, 164, 303, "You can control falling blocks and fill");
					ascii(c, i, 164, 323, "the rows, then the rows \"mined\" and you");
					ascii(c, i, 164, 343, "should be rewarded for it.");
					ascii(c, i, 164, 363, "Your operations are these: move horizon-");
					ascii(c, i, 164, 383, "tally, rotate, push onto stack and pop.");
					ascii(c, i, 164, 423, "Blocks contains 7 types:");
					ascii(c, i, 164, 443, " -Quartz (mainly appears)");
					ascii(c, i, 164, 463, " -Jewel (sometimes appears)");
					ascii(c, i, 164, 483, " -Hidden Treasure (rarely appears)");
					ascii(c, i, 164, 503, " -Found Treasure (result of operations)");
					ascii(c, i, 164, 523, " -Active Bomb (rarely appears)");
					ascii(c, i, 164, 543, " -Defused Bomb (result of landing)");
					ascii(c, i, 164, 563, " -Debris (result of operations)");
					ascii(c, i, 164, 603, "An Active Bomb which pushed onto stack");
					ascii(c, i, 164, 623, "will explode and change other blocks");
					ascii(c, i, 164, 643, "into Debris.");
					ascii(c, i, 164, 663, "A Found Treasure can be found by pushing");
					ascii(c, i, 164, 683, "a Hidden Treasure onto stack.");
					ascii(c, i, 164, 723, "Details of these are the next page.");
					ascii(c, i, 164, 853, "["+ gcde[KEY_CXL] +"] Back ["+ gcde[KEY_CVR] +"] Next");
					break;
					
				case 1:
					ascii(c, i, 164, 303, "Blocks (1/2)");
					ascii(c, i, 164, 323, "### Quartz");
					ascii(c, i, 164, 343, "    RATE: **--- / HEAL: **---");
					ascii(c, i, 164, 363, "    PENALTY: *----");
					ascii(c, i, 164, 403, "### Jewel");
					ascii(c, i, 164, 423, "    RATE: ***-- / HEAL: ***--");
					ascii(c, i, 164, 443, "    PENALTY: **---");
					ascii(c, i, 164, 483, "### Hidden Treasure");
					ascii(c, i, 164, 503, "    RATE: *---- / HEAL: *----");
					ascii(c, i, 164, 523, "    PENALTY: *----");
					ascii(c, i, 164, 563, "### Found Treasure");
					ascii(c, i, 164, 583, "    RATE: ***** / HEAL: *****");
					ascii(c, i, 164, 603, "    PENALTY: ***--");
					ascii(c, i, 164, 643, "### Active Bomb");
					ascii(c, i, 164, 663, "    RATE: ----- / HEAL: -----");
					ascii(c, i, 164, 683, "    PENALTY: *****");
					ascii(c, i, 164, 853,
							"["+ gcde[KEY_CXL] +"] Back ["+ gcde[KEY_CVL] +"] Prev ["+ gcde[KEY_CVR] +"] Next");
					drawingFrameBarrier(c, i);
					LOCAL.srcCoord(0, 24).dstCoord(164, 325)
							.cpyRegion(24, 24).blendConstant(ShaderUBOInfo.BLEND_COLOR).copy(c, i);
					LOCAL.srcCoord(0, 48).dstCoord(164, 405).color(i_t1_color).copy(c, i);
					LOCAL.srcCoord(0, 72).dstCoord(164, 485).color(i_t2_color).copy(c, i);
					LOCAL.srcCoord(0, 96).dstCoord(164, 565).color(i_tg_color).copy(c, i);	
					LOCAL.srcCoord(0, 120).dstCoord(164, 645)
							.blendConstant(ShaderUBOInfo.BLEND_DST).color(0).copy(c, i);	
					break;
				case 2:
					ascii(c, i, 164, 303, "Blocks (2/2)");
					ascii(c, i, 164, 323, "### Defused Bomb");
					ascii(c, i, 164, 343, "    RATE: ****- / HEAL: ****-");
					ascii(c, i, 164, 363, "    PENALTY: **---");
					ascii(c, i, 164, 403, "### Debris");
					ascii(c, i, 164, 423, "    RATE: ----- / HEAL: *----");
					ascii(c, i, 164, 443, "    PENALTY: *****");
					ascii(c, i, 164, 483, "Treasures and Bombs do not appear in the");
					ascii(c, i, 164, 503, "same group.");
					ascii(c, i, 164, 853, "["+ gcde[KEY_CXL] +"] Back ["+ gcde[KEY_CVL] +"] Prev");
					drawingFrameBarrier(c, i);
					LOCAL.srcCoord(0, 144).dstCoord(164, 325).cpyRegion(24, 24).copy(c, i);	
					LOCAL.srcCoord(0, 168).dstCoord(164, 405).copy(c, i);
					break;
				}
			} else if(g_stat.code == GSTAT_T_I2_SYS) {
				LOCAL.srcCoord(651, 716).dstCoord(164, 243).cpyRegion(244, 52).copy(c, i);
				ascii(c, i, 164, 303, "GPU Name:");
				ascii(c, i, 172, 323, getGPUName());
				ascii(c, i, 164, 363, "GPU Driver Version: "+ getDriverVersion());
				ascii(c, i, 164, 403, "Vulkan API Version: "+ getAPIVersion());
				ascii(c, i, 164, 443, "JVM Version: "+ System.getProperty("java.vm.version"));
				ascii(c, i, 164, 483, "Shaft "+ SHAFT_VERSION);
				ascii(c, i, 164, 853, "["+ gcde[KEY_CXL] +"] Back");
			} else if(g_stat.code == GSTAT_T_I2_LIC) {
				LOCAL.srcCoord(651, 768).dstCoord(164, 243).cpyRegion(244, 52).copy(c, i);
				ascii(c, i, 164, 303, "Copyright (C)2024 Virtual Causality");
				ascii(c, i, 164, 343, "Terms of Use and more info: index.html");
				ascii(c, i, 164, 383, "OSS used in this game:");
				ascii(c, i, 172, 403, "Java");
				ascii(c, i, 172, 423, "LWJGL");
				ascii(c, i, 172, 443, "JOML");
			} else if(g_stat.code == GSTAT_T_I2_STA) {
				LOCAL.srcCoord(651, 820).dstCoord(164, 243).cpyRegion(244, 52).copy(c, i);
				ascii(c, i, 164, 303, "Images, Sounds, Programming and others");
				LOCAL.srcCoord(363, 0).dstCoord(180, 329).cpyRegion(288, 336).copy(c, i);
				if(p_stat.code == GSTAT_P_S) {
					ascii(c, i, 164, 853, "["+ gcde[KEY_DEC] +"] Title");
				} else {
					ascii(c, i, 164, 853, "["+ gcde[KEY_CXL] +"]["+ gcde[KEY_DEC] +"] Back");
				}
			}
		}
		drawingFrameBarrier(c, i);
		if(hp > 45) {
			LOCAL.srcCoord(200, 865).dstCoord(226, 148).cpyRegion(hp * 220 / 10000, 6)
					.color(i_bar_color).blendConstant(ShaderUBOInfo.BLEND_COLOR).copy(c, i);
			LOCAL.color(0).blendConstant(ShaderUBOInfo.BLEND_DST);
		}
		if(g_stat.code == GSTAT_G_X) {
			for(int j = 0; j < 4; j++) {
				int k = c_y + j;
				if(keep[j] == 1 && k >= 4 && k < 32) {
					LOCAL.dstCoord(156, (k - 4) * 24 + 205).cpyRegion(336, 24).color(0x0000007F);
					rect(c, i, LOCAL);
				}
			}
			LOCAL.color(0);
		}
		drawingFrameBarrier(c, i);
	}
	
	static void copyToUniformBuffer(int imageIdx) {
		if(g_stat.code > 0) {
			updateGuide(imageIdx, gposg);
		} else {
			updateGuide(imageIdx, gposp);
		}
		updateControl(imageIdx, kcle);
		if(g_stat.code > 0 || g_stat.code == GSTAT_P_S || g_stat.code == GSTAT_P_D) {
			updateCells(imageIdx, 88, 520, cells);
		}
		if(sgsw[SEGMENT_CLOCK_IDX] == 1) {
			var c = Calendar.getInstance();
			turnOnSegment(SEGMENT_TIME_H, SEGMENT_TIME_H_DIGIT, c.get(Calendar.HOUR_OF_DAY), false);
			turnOnSegment(SEGMENT_TIME_M, SEGMENT_TIME_M_DIGIT, c.get(Calendar.MINUTE), true);
			turnOnSegment(SEGMENT_TIME_S, SEGMENT_TIME_S_DIGIT, c.get(Calendar.SECOND), true);
		} else {
			turnOffSegment(SEGMENT_TIME_H, SEGMENT_TIME_H_DIGIT);
			turnOffSegment(SEGMENT_TIME_M, SEGMENT_TIME_M_DIGIT);
			turnOffSegment(SEGMENT_TIME_S, SEGMENT_TIME_S_DIGIT);
		}
		if(sgsw[SEGMENT_SCORE_IDX] == 1) {
			turnOnSegment(SEGMENT_SCORE, SEGMENT_SCORE_DIGIT, score, false);
		} else {
			turnOffSegment(SEGMENT_SCORE, SEGMENT_SCORE_DIGIT);
		}
		if(sgsw[SEGMENT_LEVEL_IDX] == 1) {
			turnOnSegment(SEGMENT_LEVEL, SEGMENT_LEVEL_DIGIT, g_level + 1, false);
		} else {
			turnOffSegment(SEGMENT_LEVEL, SEGMENT_LEVEL_DIGIT);
		}
		if(sgsw[SEGMENT_HP_IDX] == 1) {
			turnOnSegment(SEGMENT_HP_REM, SEGMENT_HP_REM_DIGIT, hp, false);
			turnOnSegment(SEGMENT_HP_MAX, SEGMENT_HP_MAX_DIGIT, PLAYER_HP_X, true);
		} else {
			turnOffSegment(SEGMENT_HP_REM, SEGMENT_HP_REM_DIGIT);
			turnOffSegment(SEGMENT_HP_MAX, SEGMENT_HP_MAX_DIGIT);
		}
		if(sgsw[SEGMENT_WAIT_IDX] == 1) {
			int t = dn_cnt < 0 ? 0 : dn_cnt;
			turnOnSegment(SEGMENT_WAIT_SEC, SEGMENT_WAIT_SEC_DIGIT, t / 1000, false);
			turnOnSegment(SEGMENT_WAIT_MILS, SEGMENT_WAIT_MILS_DIGIT, (t % 1000) / 10, true);
		} else {
			turnOffSegment(SEGMENT_WAIT_SEC, SEGMENT_WAIT_SEC_DIGIT);
			turnOffSegment(SEGMENT_WAIT_MILS, SEGMENT_WAIT_MILS_DIGIT);
		}
		if(sgsw[SEGMENT_LINES_IDX] == 1) {
			turnOnSegment(SEGMENT_LINES, SEGMENT_LINES_DIGIT, l_cnt, false);
		} else {
			turnOffSegment(SEGMENT_LINES, SEGMENT_LINES_DIGIT);
		}
		if(sgsw[SEGMENT_COUNT_IDX] == 1) {
			turnOnSegment(SEGMENT_BG, SEGMENT_BG_DIGIT, ccnt[C_BG], false);
			turnOnSegment(SEGMENT_T1, SEGMENT_T1_DIGIT, ccnt[C_T1], false);
			turnOnSegment(SEGMENT_TG, SEGMENT_TG_DIGIT, ccnt[C_TG], false);
		} else {
			turnOffSegment(SEGMENT_BG, SEGMENT_BG_DIGIT);
			turnOffSegment(SEGMENT_T1, SEGMENT_T1_DIGIT);
			turnOffSegment(SEGMENT_TG, SEGMENT_TG_DIGIT);
		}
		updateSegment(imageIdx, 0, 47, segment);
		
	}
	
	private static void createShaderResources() {
		regions = new ArrayList<>(17);
		
		// BACKGROUND
		SCREEN = new ShaderUBOInfo()
				.srcCoord(0, 0)
				.dstCoord(0, 0)
				.cpyRegion(800, 900)
				.color(0x00000000)
				.blendConstant(ShaderUBOInfo.BLEND_COLOR);
		regions.add(SCREEN);
		
		// PANELS
		PANELS = new ShaderUBOInfo[2];
		PANELS[0] = new ShaderUBOInfo()
				.srcCoord(24, 0)
				.dstCoord(156, 50)
				.cpyRegion(339, 150)
				.color(0x00000000)
				.blendConstant(ShaderUBOInfo.BLEND_DST);
		PANELS[1] = new ShaderUBOInfo()
				.srcCoord(363, 336)
				.dstCoord(500, 50)
				.cpyRegion(240, 461)
				.color(0x00000000)
				.blendConstant(ShaderUBOInfo.BLEND_DST);
		for(var i : PANELS) {
			regions.add(i);
		}
		
		// SHADOWS
		SHADOWS = new ShaderUBOInfo[9];
		SHADOWS[0] = new ShaderUBOInfo()
				.dstCoord(55, 104)
				.cpyRegion(96, 96)
				.color(0xFFFFFF3F);
		SHADOWS[1] = new ShaderUBOInfo()
				.dstCoord(55, 208)
				.cpyRegion(96, 96)
				.color(0xFFFFFF3F);
		SHADOWS[2] = new ShaderUBOInfo()
				.dstCoord(55, 312)
				.cpyRegion(96, 96)
				.color(0xFFFFFF3F);
		SHADOWS[3] = new ShaderUBOInfo()
				.dstCoord(55, 416)
				.cpyRegion(96, 96)
				.color(0xFFFFFF3F);
		SHADOWS[4] = new ShaderUBOInfo()
				.dstCoord(55, 520)
				.cpyRegion(96, 96)
				.color(0xFFFFFF3F);
		SHADOWS[5] = new ShaderUBOInfo()
				.dstCoord(503, 520)
				.cpyRegion(96, 96)
				.color(0xFFFFFF3F);
		SHADOWS[6] = new ShaderUBOInfo()
				.dstCoord(503, 624)
				.cpyRegion(96, 96)
				.color(0xFFFFFF3F);
		SHADOWS[7] = new ShaderUBOInfo()
				.dstCoord(503, 728)
				.cpyRegion(96, 96)
				.color(0xFFFFFF3F);
		SHADOWS[8] = new ShaderUBOInfo()
				.dstCoord(159, 208)
				.cpyRegion(336, 672)
				.color(0xFFFFFF3F);
		for(var i : SHADOWS) {
			regions.add(i);
		}
		
		MENU = new ShaderUBOInfo()
				.srcCoord(24, 150)
				.dstCoord(156, 205)
				.cpyRegion(339, 675)
				.color(0x00000000)
				.blendConstant(ShaderUBOInfo.BLEND_DST);
		regions.add(MENU);
		
		BG_CNT_T1 = new ShaderUBOInfo()
				.srcCoord(603, 768)
				.dstCoord(505, 211)
				.cpyRegion(48, 48)
				.blendConstant(ShaderUBOInfo.BLEND_COLOR);
		regions.add(BG_CNT_T1);
		BG_CNT_TG = new ShaderUBOInfo()
				.srcCoord(603, 816)
				.dstCoord(505, 263)
				.cpyRegion(48, 48)
				.blendConstant(ShaderUBOInfo.BLEND_COLOR);
		regions.add(BG_CNT_TG);
		BG_HP_BAR = new ShaderUBOInfo()
				.srcCoord(200, 871)
				.dstCoord(226, 148)
				.cpyRegion(220, 6)
				.blendConstant(ShaderUBOInfo.BLEND_DST);
		regions.add(BG_HP_BAR);
		
		LOCAL = new ShaderUBOInfo()
				.blendConstant(ShaderUBOInfo.BLEND_DST);
	}
	
	private static void turnOnSegment(int value, int digit, int newValue, boolean zeroPadding) {
		int nv = newValue;
		int nw;
		int idx = value;
		boolean flg = zeroPadding;
		int div = 1;
		for(int i = 1; i < digit; i++) {
			div *= 10;
		}
		for(int i = 0; i < digit - 1; i++) {
			if(flg) {
				segment[idx] = nv / div;
			} else {
				nw = nv / div;
				segment[idx] = nw == 0 ? 10 : nw;
				if(nw != 0) {
					flg = true;
				}
			}
			nv %= div;
			div /= 10;
			idx++;
		}
		segment[idx] = nv;
	}
	
	private static void turnOffSegment(int value, int digit) {
		for(int i = value; i < value + digit; i++) {
			segment[i] = 10;
		}
	}
	
	private static void writeLog() {
		var sb = new StringBuilder(860);
		var cd = Calendar.getInstance();
		sb.append("SHAFT ");
		sb.append(String.format("%d/%02d/%02d %02d:%02d:%02d",
				cd.get(Calendar.YEAR), cd.get(Calendar.MONTH) + 1, cd.get(Calendar.DAY_OF_MONTH),
				cd.get(Calendar.HOUR_OF_DAY), cd.get(Calendar.MINUTE), cd.get(Calendar.SECOND)));
		sb.append(String.format("\n\nSCORE %8d\n", score));
		sb.append(String.format("   HP %8d\n", hp));
		sb.append(String.format("LINES %8d\n", l_cnt));
		sb.append(String.format("B CNT %8d\n", ccnt[C_BG]));
		sb.append(String.format("J CNT %8d\n", ccnt[C_T1]));
		sb.append(String.format("T CNT %8d\n\n", ccnt[C_TG]));
		
		sb.append("CONTROL\n");
		for(int i = 0; i < 16; i += 4) {
			for(int j = 0; j < 4; j++) {
				sb.append(cellG[cells[i + j] & 0x000000FF]);
			}
			sb.append("\n");
		}
		sb.append("\nQUEUE_0_1_2_3_4_G\n");
		for(int i = 0; i < 16; i += 4) {
			for(int j = 0; j < 4; j++) {
				sb.append(cellG[cells[QUEUE_0 + i + j] & 0x000000FF]);
			}
			sb.append(" ");
			for(int j = 0; j < 4; j++) {
				sb.append(cellG[cells[QUEUE_1 + i + j] & 0x000000FF]);
			}
			sb.append(" ");
			for(int j = 0; j < 4; j++) {
				sb.append(cellG[cells[QUEUE_2 + i + j] & 0x000000FF]);
			}
			sb.append(" ");
			for(int j = 0; j < 4; j++) {
				sb.append(cellG[cells[QUEUE_3 + i + j] & 0x000000FF]);
			}
			sb.append(" ");
			for(int j = 0; j < 4; j++) {
				sb.append(cellG[cells[QUEUE_4 + i + j] & 0x000000FF]);
			}
			sb.append(" ");
			if(qg_isVacant) {
				for(int j = 0; j < 4; j++) {
					sb.append(cellG[C_VA]);
				}
			} else {
				for(int j = 0; j < 4; j++) {
					sb.append(cellG[cells[QUEUE_G + i + j] & 0x000000FF]);
				}
			}
			sb.append("\n");
		}
		sb.append("\nSTACK_0_1_2\n");
		for(int i = 0; i < 16; i += 4) {
			for(int j = 0; j < 4; j++) {
				sb.append(cellG[cells[STACK_0 + i + j] & 0x000000FF]);
			}
			sb.append(" ");
			for(int j = 0; j < 4; j++) {
				sb.append(cellG[cells[STACK_1 + i + j] & 0x000000FF]);
			}
			sb.append(" ");
			for(int j = 0; j < 4; j++) {
				sb.append(cellG[cells[STACK_2 + i + j] & 0x000000FF]);
			}
			sb.append("\n");
		}
		sb.append("\nBOARD\n");
		for(int i = 0; i < 56; i += 14) {
			for(int j = 0; j < 14; j++) {
				sb.append(cellG[cells[BOARD_H + i + j] & 0x000000FF]);
			}
			sb.append("\n");
		}
		sb.append("==============\n");
		for(int i = 0; i < 392; i += 14) {
			for(int j = 0; j < 14; j++) {
				sb.append(cellG[cells[BOARD_V + i + j] & 0x000000FF]);
			}
			sb.append("\n");
		}
		String fn = String.format("l%d%02d%02d_%02d%02d%02d.txt",
				cd.get(Calendar.YEAR),
				cd.get(Calendar.MONTH) + 1,
				cd.get(Calendar.DAY_OF_MONTH),
				cd.get(Calendar.HOUR_OF_DAY),
				cd.get(Calendar.MINUTE),
				cd.get(Calendar.SECOND));
		String[] lines = sb.toString().split("\n");
		try(var io = Files.newBufferedWriter(Paths.get(fn), StandardOpenOption.CREATE)){
			for(var l : lines) {
				io.write(l);
				io.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void initSound() {
		sdl = new SourceDataLine[2];
		boolean errFlg = false;
		try {
			var pFormat = new AudioFormat[1];
			SE_DEC = readSoundFile("assets/se_ok.wav", pFormat);
			SE_LAND = readSoundFile("assets/se_land2.wav", null);
			SE_STACK = readSoundFile("assets/se_stack4.wav", null);
			SE_CTRL = readSoundFile("assets/se_rot2.wav", null);
			SE_CV = readSoundFile("assets/se_cur2.wav", null);
			SE_LVUP = readSoundFile("assets/se_lvup.wav", null);
			SE_CLEAR = readSoundFile("assets/se_clr.wav", null);
			SE_GMOVR = readSoundFile("assets/se_gov.wav", null);
			SE_CXL = reverseSound(SE_DEC, pFormat[0]);
			SE_NUL = reverseSound(SE_CV, pFormat[0]);
			SE_STACK2 = reverseSound(SE_STACK, pFormat[0]);
			
			// ***
			
			sdl[0] = AudioSystem.getSourceDataLine(pFormat[0]);
			sdl[1] = AudioSystem.getSourceDataLine(pFormat[0]);
			
			sdl[0].open();
			sdl[1].open();
			
			sec = new FloatControl[2];
			// System.out.println(Arrays.toString(sdl[0].getControls()));
			try {
				sec[0] = (FloatControl)sdl[0].getControl(FloatControl.Type.MASTER_GAIN);
				sec[1] = (FloatControl)sdl[1].getControl(FloatControl.Type.MASTER_GAIN);
				
				volume(sosw[0]);
			} catch (IllegalArgumentException ie) {
				ie.printStackTrace();
				errFlg = true;
			}
			
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			errFlg = true;
		} catch (IOException e) {
			e.printStackTrace();
			errFlg = true;
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			errFlg = true;
		}
		if(errFlg) {
			throw new RuntimeException("Failed to init sound env");
		}
	}
	
	private static SE readSoundFile(String src, AudioFormat[] f) throws UnsupportedAudioFileException, IOException {
		try(var ais = AudioSystem.getAudioInputStream(new File(src))){
			var se = new SE(new byte[(int)(ais.getFrameLength() * ais.getFormat().getFrameSize())]);
			se.length = ais.read(se.buf);
			if(f != null && f.length > 0) {
				f[0] = ais.getFormat();
			}
			return se;
		}
	}
	
	private static SE reverseSound(SE src, AudioFormat f) {
		var se = new SE(new byte[src.buf.length]);
		se.length = src.length;
		
		long j = src.length - f.getFrameSize();
		long k = f.getFrameSize();
		int cnt = 0;
		for(long j2 = j; j2 >= 0; j2 -= k) {
			for(long k2 = 0; k2 < k; k2++) {
				se.buf[cnt] = src.buf[(int)(j2 + k2)];
				cnt++;
			}
		}
		
		return se;
	}
	
	private static void play(SE data) {
		sdl[lineIdx].stop();
		sdl[lineIdx].flush();
		sdl[lineIdx].write(data.buf, 0, data.length);
		sdl[lineIdx].start();
		lineIdx = (lineIdx + 1) & 1;
	}
	
	private static void volume(int value) {
		if(value == 0) {
			sec[0].setValue(sec[0].getMinimum());
			sec[1].setValue(sec[1].getMinimum());
		} else {
			float v = (float)(2.0 - Math.log10(value * 0.9 + 10.0));
			sec[0].setValue(sec[0].getMinimum() * v);
			sec[1].setValue(sec[1].getMinimum() * v);
		}
	}
	
	static void free() {
		var f = new File(CFG_NAME);
		boolean g = true;
		if(f.exists() && !f.canWrite()) {
			g = false;
		}
		if(g) {
			try(var fw = new FileWriter(CFG_NAME)){
				var sb = new StringBuilder(26);
				for(int i = 0; i < sgsw.length; i++) {
					sb.append(sgsw[i]);
					sb.append(" ");
				}
				for(int i = 0; i < scsw.length; i++) {
					sb.append(scsw[i]);
					sb.append(" ");
				}
				for(int i = 0; i < sosw.length; i++) {
					sb.append(sosw[i]);
					sb.append(" ");
				}
				for(int i = 0; i < ctsw.length; i++) {
					sb.append(ctsw[i]);
					sb.append(" ");
				}
				sb.append("\n");
				fw.write(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		sdl[0].drain();
		sdl[1].drain();
		sdl[0].close();
		sdl[1].close();
		regions.forEach(i -> i.free());
	}
}
