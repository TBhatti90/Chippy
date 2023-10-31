package com.taaha.chippy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

public class Chippy extends ApplicationAdapter {
	// RAM
	private char[] mem;
	// Registers
	private char[] V;
	// Stores addresses, only lowest 12 bits are used
	private char I;
	// Delay Register
	private char delay;
	// Sound Register
	private char sound;
	// Stack
	private char[] stack;
	// Program Counter, stores currently executing address
	private char PC;
	// Stack pointer, points to top of stack
	private char SP;
	// Display
	private boolean[][] display;
	// X-Coordinate for drawing
	private char xCoord;
	// Y-Coordinate for drawing
	private char yCoord;
	// Instruction
	private char instruction;
	// Keypad
	private char[] keypad;
	// Camera
	private OrthographicCamera camera;
	// Drawing pixels
	private ShapeRenderer pixel;

	@Override
	public void create () {
		mem = new char[4096];
		V = new char[16];
		I = 0x0;
		delay = 0;
		sound = 0;
		stack = new char[16];
		PC = 0x0;
		SP = 0;
		display = new boolean[64][32];
		xCoord = 0x0;
		yCoord = 0x0;
		instruction = 0x0;
		keypad = new char[16];

		camera = new OrthographicCamera(Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());
		camera.setToOrtho(true, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());

		pixel = new ShapeRenderer();

		//Initialization
		initialize();
	}

	private void font() {
		//0
		mem[0x50] = (char) 0xF0;
		mem[0x51] = (char) 0x90;
		mem[0x52] = (char) 0x90;
		mem[0x53] = (char) 0x90;
		mem[0x54] = (char) 0xF0;
		//1
		mem[0x55] = 0x20;
		mem[0x56] = 0x60;
		mem[0x57] = 0x20;
		mem[0x58] = 0x20;
		mem[0x59] = 0x70;
		//2
		mem[0x5A] = (char) 0xF0;
		mem[0x5B] = 0x10;
		mem[0x5C] = (char) 0xF0;
		mem[0x5D] = (char) 0x80;
		mem[0x5E] = (char) 0xF0;
		//3
		mem[0x5F] = (char) 0xF0;
		mem[0x60] = 0x10;
		mem[0x61] = (char) 0xF0;
		mem[0x62] = 0x10;
		mem[0x63] = (char) 0xF0;
		//4
		mem[0x64] = (char) 0x90;
		mem[0x65] = (char) 0x90;
		mem[0x66] = (char) 0xF0;
		mem[0x67] = 0x10;
		mem[0x68] = 0x10;
		//5
		mem[0x69] = (char) 0xF0;
		mem[0x6A] = (char) 0x80;
		mem[0x6B] = (char) 0xF0;
		mem[0x6C] = 0x10;
		mem[0x6D] = (char) 0xF0;
		//6
		mem[0x6E] = (char) 0xF0;
		mem[0x6F] = (char) 0x80;
		mem[0x70] = (char) 0xF0;
		mem[0x71] = (char) 0x90;
		mem[0x72] = (char) 0xF0;
		//7
		mem[0x73] = (char) 0xF0;
		mem[0x74] = 0x10;
		mem[0x75] = 0x20;
		mem[0x76] = 0x40;
		mem[0x77] = 0x40;
		//8
		mem[0x78] = (char) 0xF0;
		mem[0x79] = (char) 0x90;
		mem[0x7A] = (char) 0xF0;
		mem[0x7B] = (char) 0x90;
		mem[0x7C] = (char) 0xF0;
		//9
		mem[0x7D] = (char) 0xF0;
		mem[0x7E] = (char) 0x90;
		mem[0x7F] = (char) 0xF0;
		mem[0x80] = 0x10;
		mem[0x81] = (char) 0xF0;
		//A
		mem[0x82] = (char) 0xF0;
		mem[0x83] = (char) 0x90;
		mem[0x84] = (char) 0xF0;
		mem[0x85] = (char) 0x90;
		mem[0x86] = (char) 0x90;
		//B
		mem[0x87] = (char) 0xE0;
		mem[0x88] = (char) 0x90;
		mem[0x89] = (char) 0xE0;
		mem[0x8A] = (char) 0x90;
		mem[0x8B] = (char) 0xE0;
		//C
		mem[0x8C] = (char) 0xF0;
		mem[0x8D] = (char) 0x80;
		mem[0x8E] = (char) 0x80;
		mem[0x8F] = (char) 0x80;
		mem[0x90] = (char) 0xF0;
		//D
		mem[0x91] = (char) 0xE0;
		mem[0x92] = (char) 0x90;
		mem[0x93] = (char) 0x90;
		mem[0x94] = (char) 0x90;
		mem[0x95] = (char) 0xE0;
		//E
		mem[0x96] = (char) 0xF0;
		mem[0x97] = (char) 0x80;
		mem[0x98] = (char) 0xF0;
		mem[0x99] = (char) 0x80;
		mem[0x9A] = (char) 0xF0;
		//F
		mem[0x9B] = (char) 0xF0;
		mem[0x9C] = (char) 0x80;
		mem[0x9D] = (char) 0xF0;
		mem[0x9E] = (char) 0x80;
		mem[0x9F] = (char) 0x80;
	}

	private void game() {
		FileHandle fileIn = Gdx.files.internal("IBM Logo.ch8");
		byte[] temp = new byte[mem.length];
		fileIn.readBytes(temp, 0x200, (int)fileIn.length());
		for (int i = 0x200; i < mem.length; ++i) {
			mem[i] = (char)(temp[i]&0xFF);
		}
	}

	private void memDump() {
		//Memory dump
		FileHandle fileOut = Gdx.files.local("mem.ch8");
		byte[] temp = new byte[mem.length];
		for (int i = 0; i < mem.length; ++i) {
			temp[i] = (byte) mem[i];
		}
		fileOut.writeBytes(temp, false);
	}

	private void initialize() {
		//Initialization process
		//Initialize font into memory
		font();
		//Placing game into memory
		game();
		//Setting PC to proper memory location
		PC = 0x200;
		//Setting SP to proper stack location
		SP = 0x0F;
	}

	private void opcodeFetch() {
		//Fetch
		char byte1 = mem[PC];
		char byte2 = mem[PC + 1];
		byte1 = (char) (byte1 << 8);
		instruction = (char) (byte1 | byte2);
		PC = (char) (PC + 2);
	}

	private void opcodeDecodeAndExecute() {
		//Decode
		//Extract bytes from instruction.
		char nibble = (char) ((instruction & 0xF000) >>> 12);
		char X = (char) ((instruction & 0x0F00) >>> 8);
		char Y = (char) ((instruction & 0x00F0) >>> 4);
		char N = (char) (instruction & 0x000F);
		char NN = (char) (instruction & 0x00FF);
		char NNN = (char) (instruction & 0x0FFF);
		//Decode and Execute
		switch (nibble) {
			case (0):
				//Call routine at NNN
				if (X != 0) {
				}
				//Returning from subroutine
				if (N != 0) {
				}
				//Clearing screen
				display = new boolean[64][32];
				break;
			// Jump to address NNN
			case (1):
				PC = NNN;
				break;
			case (2):
				break;
			case (3):
				break;
			case (4):
				break;
			case (5):
				break;
			//Sets VX to NN
			case (6):
				V[X] = NN;
				break;
			//Adds NN to VX. (Carry flag is not changed).
			case (7):
				V[X] += NN;
				break;
			case (8):
				break;
			case (9):
				break;
			//Sets I to NNN
			case (0xA):
				I = NNN;
				break;
			case (0xB):
				break;
			case (0xC):
				break;
			//Draws a sprite at the coordinates (VX,VY), with a width of
			// 8 pixels and a height of N pixels.
			case (0xD):
				yCoord = (char) (V[Y] % display[0].length);
				for (int j = 0; j < N; ++j) {
					xCoord = (char) (V[X] % display.length);
					char sprite = mem[I + j];
					char divisor = 0x80;
					for (int k = 8; k > 0; --k) {
						char pixel = (char) ((sprite & divisor) >>> (k - 1));
						divisor /= 2;
						if (pixel == 1 && display[xCoord][yCoord]) {
							display[xCoord][yCoord] = false;
							V[0xF] = 1;
						}
						if (pixel == 1 && !display[xCoord][yCoord]) {
							display[xCoord][yCoord] = true;
							V[0xF] = 0;
						}
						++xCoord;
						if (xCoord > display.length - 1) {
							break;
						}
					}
					++yCoord;
					if (yCoord > display[0].length - 1) {
						break;
					}
				}
				break;
			case (0xE):
				break;
			case (0xF):
				break;
			default:
				break;
		}
	}

	@Override
	public void render () {
		opcodeFetch();
		opcodeDecodeAndExecute();

		ScreenUtils.clear(0, 0, 0, 1);

		camera.update();

		for (int i = 0; i < display[0].length; ++i) {
			for (int j = 0; j < display.length; ++j) {
				if (display[j][i]) {
					pixel.begin(ShapeRenderer.ShapeType.Point);
					pixel.setColor(1, 1, 1, 1);
					pixel.point(i, j,0);
					pixel.end();
				}
			}
		}
	}
	
	@Override
	public void dispose () {
	}
}
