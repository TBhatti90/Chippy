package com.taaha.chippy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.VisUI.SkinScale;
import com.kotcrab.vis.ui.util.TableUtils;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileTypeFilter;
import com.kotcrab.vis.ui.widget.file.SingleFileChooserListener;

import java.util.Random;
import java.util.Stack;

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
	private Stack<Character> stack;
	// Program Counter, stores currently executing address
	private char PC;
	// Display
	private boolean[][] display;
	// X-Coordinate for drawing
	private char xCoord;
	// Y-Coordinate for drawing
	private char yCoord;
	// Instruction
	private char instruction;
	// Camera
	private OrthographicCamera camera;
	// Emulator Viewport
	private FitViewport fitViewport;
	// GUI Viewport
	private ScreenViewport screenViewport;
	// Drawing pixels
	private ShapeRenderer shape;
	// Draw flag
	private boolean draw;
	// Invalid opcode flag
	private boolean invalid;
	// Buzzer
	private Sound buzzer;
	// GUI flag
	private boolean gui;
	// Initialize flag
	private boolean initial;
	// Game file
	private FileHandle fileIn;
	// GUI stage
	private Stage stage;
	// GUI File Chooser
	private FileChooser chooser;
	// GUI File Typefilter
	private FileTypeFilter typeFilter;

	@Override
	public void create () {
		// Core
		mem = new char[4096];
		V = new char[16];
		I = 0x0;
		delay = 0;
		sound = 0;
		stack = new Stack<>();
		PC = 0x0;
		display = new boolean[64][32];
		xCoord = 0x0;
		yCoord = 0x0;
		instruction = 0x0;
		draw = false;
		invalid = false;
		gui = true;
		initial = true;
		fileIn = null;

		// Sound
		buzzer = Gdx.audio.newSound(Gdx.files.internal("blip.wav"));

		// GUI
		camera = new OrthographicCamera();
		camera.position.set(camera.viewportWidth / 2,
				camera.viewportHeight / 2, 0);

		fitViewport = new FitViewport(display.length, display[0].length,
				camera);
		screenViewport = new ScreenViewport(camera);

		// Begin layout
		VisUI.load(SkinScale.X1);

		stage = new Stage(screenViewport);
		// GUI Table
		Table root = new Table();
		root.setFillParent(true);
		stage.addActor(root);

		Gdx.input.setInputProcessor(stage);

		// GUI Menu Bar
		MenuBar menuBar = new MenuBar();
		root.add(menuBar.getTable()).expandX().fillX().row();
		root.add().expand().fill();

		Menu fileMenu = new Menu("File");
		Menu helpMenu = new Menu("Help");

		FileChooser.setDefaultPrefsName("com.taaha.chippy.filechooser");
		FileChooser.setSaveLastDirectory(true);
		chooser = new FileChooser(FileChooser.Mode.OPEN);
		typeFilter = new FileTypeFilter(false);
		typeFilter.addRule("Chip-8 ROM (*.ch8)", "ch8");
		fileMenu.addItem(new MenuItem("Open",
				new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
						// GUI File Chooser
						chooser.setSelectionMode(FileChooser.SelectionMode.FILES_AND_DIRECTORIES);
						chooser.setFavoriteFolderButtonVisible(true);

						TableUtils.setSpacingDefaults(chooser);

						chooser.setSize(Gdx.graphics.getWidth()/1.25f,
								Gdx.graphics.getHeight()/1.25f);
						chooser.setResizable(true);
						chooser.setFileTypeFilter(typeFilter);

						chooser.setListener(new SingleFileChooserListener() {
							@Override
							protected void selected(FileHandle file) {
								fileIn = file;
							}
						});

						stage.addActor(chooser);
					}
				}).setShortcut(
				"Ctrl + O"));
		fileMenu.addItem(new MenuItem("Close",
				new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
						System.exit(0);
					}
				}).setShortcut(
				"Ctrl + Q"));
		helpMenu.addItem(new MenuItem("About", new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Dialogs.showOKDialog(stage, "About", "Chippy Alpha");
			}
		}));
		menuBar.addMenu(fileMenu);
		menuBar.addMenu(helpMenu);

		shape = new ShapeRenderer();
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

	private char keypad(char keypad) {
		char qwerty;
		switch (keypad) {
			case 0:
				qwerty = Input.Keys.X;
				break;
			case 1:
				qwerty = Input.Keys.NUM_1;
				break;
			case 2:
				qwerty = Input.Keys.NUM_2;
				break;
			case 3:
				qwerty = Input.Keys.NUM_3;
				break;
			case 4:
				qwerty = Input.Keys.Q;
				break;
			case 5:
				qwerty = Input.Keys.W;
				break;
			case 6:
				qwerty = Input.Keys.E;
				break;
			case 7:
				qwerty = Input.Keys.A;
				break;
			case 8:
				qwerty = Input.Keys.S;
				break;
			case 9:
				qwerty = Input.Keys.D;
				break;
			case 0xA:
				qwerty = Input.Keys.Z;
				break;
			case 0xB:
				qwerty = Input.Keys.C;
				break;
			case 0xC:
				qwerty = Input.Keys.NUM_4;
				break;
			case 0xD:
				qwerty = Input.Keys.R;
				break;
			case 0xE:
				qwerty = Input.Keys.F;
				break;
			case 0xF:
				qwerty = Input.Keys.V;
				break;
			default:
				qwerty = Input.Keys.P;
				break;
		}
		return qwerty;
/*
		keypad[0] = Input.Keys.X;
		keypad[1] = Input.Keys.NUM_1;
		keypad[2] = Input.Keys.NUM_2;
		keypad[3] = Input.Keys.NUM_3;
		keypad[4] = Input.Keys.Q;
		keypad[5] = Input.Keys.W;
		keypad[6] = Input.Keys.E;
		keypad[7] = Input.Keys.A;
		keypad[8] = Input.Keys.S;
		keypad[9] = Input.Keys.D;
		keypad[0xA] = Input.Keys.Z;
		keypad[0xB] = Input.Keys.C;
		keypad[0xC] = Input.Keys.NUM_4;
		keypad[0xD] = Input.Keys.R;
		keypad[0xE] = Input.Keys.F;
		keypad[0xF] = Input.Keys.V;
*/
	}

	private void reset() {
		//Reset values
		mem = new char[4096];
		V = new char[16];
		I = 0x0;
		delay = 0;
		sound = 0;
		stack = new Stack<>();
		PC = 0x0;
		display = new boolean[64][32];
		xCoord = 0x0;
		yCoord = 0x0;
		instruction = 0x0;
		draw = false;
		invalid = false;
		gui = true;
		initial = true;
		fileIn = null;
	}

	private void initialize() {
		//Initialization process
		//Initialize font into memory
		font();
		//Placing game into memory
		game();
		//Setting PC to proper memory location
		PC = 0x200;
/*
		//Setting timers
		delay = 60;
		sound = 60;
*/
	}

	private void opcodeFetch() {
		//Fetch
		char byte1 = mem[PC];
		char byte2 = mem[PC + 1];
		byte1 = (char) (byte1 << 8);
		instruction = (char) (byte1 | byte2);
		PC += 2;
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
		char carry = 0;
		//Decode and Execute
		switch (nibble) {
			case (0):
				//Call routine at NNN
				if (X != 0) {
					stack.push(PC);
					PC = NNN;
				//Returning from subroutine
				} else if (N == 0xE) {
					PC = stack.pop();
				//Clearing screen
				} else {
					display = new boolean[64][32];
					draw = true;
				}
				break;
			//Jump to address NNN
			case (1):
				PC = NNN;
				break;
			//Calls routine at NNN
			case (2):
				stack.push(PC);
				PC = NNN;
				break;
			//Skip next instruction if VX == NN
			case (3):
				if (V[X] == NN) {
					PC += 2;
				}
				break;
			//Skip next instruction if VX != NN
			case (4):
				if (V[X] != NN) {
					PC += 2;
				}
				break;
			//Skip next instruction if VX == VY
			case (5):
				if (V[X] == V[Y]) {
					PC += 2;
				}
				break;
			//Sets VX to NN
			case (6):
				V[X] = NN;
				break;
			//Adds NN to VX. (Carry flag is not changed).
			case (7):
				V[X] += NN;
				V[X] &= 0x00FF;
				break;
			//Arithmetic and Logical operations
			case (8):
				char temp = 0;
				switch(N) {
					//Set VX to VY
					case 0:
						V[X] = V[Y];
						break;
					//Set VX to VX | VY
					case 1:
						V[X] |= V[Y];
						V[0xF] = 0;
						break;
					//Set VX to VX & VY
					case 2:
						V[X] &= V[Y];
						V[0xF] = 0;
						break;
					//Set VX to VX ^ VY
					case 3:
						V[X] ^= V[Y];
						V[0xF] = 0;
						break;
					//Set VX to VX + VY. If addition is larger than 255, set
					// VF to 1; otherwise, set VF to 0
					case 4:
						V[X] += V[Y];
						if (V[X] > 0x00FF) {
							V[0xF] = 1;
						} else {
							V[0xF] = 0;
						}
						V[X] &= 0x00FF;
						break;
					//Set VX to VX - VY. If the first operand is larger than
					// the second operand, set VF to 1; otherwise, set VF to 0
					case 5:
						temp = V[X];
						V[X] = (char) (V[X] - V[Y]);
						if (temp > V[Y]) {
							V[0xF] = 1;
						} else {
							V[0xF] = 0;
						}
						V[X] &= 0x00FF;
						break;
					//Set VX to VY. Shift VX 1-bit to the right and set VF if
					// needed
					case 6:
						V[X] = V[Y];
						carry = (char) (V[X] & 0x0001);
						V[X] = (char) (V[X] >>> 1);
						if (carry == 1) {
							V[0xF] = 1;
						} else {
							V[0xF] = 0;
						}
						break;
					//Set VX to VY - VX. If the first operand is larger than
					// the second operand, set VF to 1; otherwise, set VF to 0
					case 7:
						temp = V[X];
						V[X] = (char) (V[Y] - V[X]);
						if (V[Y] > temp) {
							V[0xF] = 1;
						} else {
							V[0xF] = 0;
						}
						V[X] &= 0x00FF;
						break;
					//Set VX to VY. Shift VX 1-bit to the left and set VF if
					// needed
					case 0xE:
						V[X] = V[Y];
						carry = (char) (V[X] & 0x0080);
						V[X] = (char) (V[X] << 1);
						V[X] &= 0x00FF;
						if (carry == 0x0080) {
							V[0xF] = 1;
						} else {
							V[0xF] = 0;
						}
						break;
					default:
						invalid = true;
						break;
				}
				break;
			//Skips next instruction if VX != VY.
			case (9):
				if (V[X] != V[Y]) {
					PC += 2;
				}
				break;
			//Sets I to NNN
			case (0xA):
				I = NNN;
				break;
			//Jumps to address NNN + V0
			case (0xB):
				PC = (char) (NNN + V[0]);
				break;
			//Sets VX to a random number AND with NN.
			case (0xC):
				Random random = new Random();
				char randomNumber = (char) (random.nextInt(0x0100));
				V[X] = (char) (randomNumber & NN);
				break;
			//Draws a sprite at the coordinates (VX,VY), with a width of
			// 8 pixels and a height of N pixels.
			case (0xD):
				draw = true;
				yCoord = (char) (V[Y] % display[0].length);
				V[0xF] = 0;
				//Height
				for (int i = 0; i < N; ++i) {
					xCoord = (char) (V[X] % display.length);
					char sprite = mem[I + i];
					char divisor = 0x80;
					//Width
					for (int j = 8; j > 0; --j) {
						char pixel = (char) ((sprite & divisor) >>> (j - 1));
						divisor /= 2;
						if (pixel == 1 && display[xCoord][yCoord]) {
							display[xCoord][yCoord] = false;
							V[0xF] = 1;
						} else if (pixel == 1 && !display[xCoord][yCoord]) {
							display[xCoord][yCoord] = true;
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
				//Skip one instruction if key corresponding to VX is pressed.
				if (Y == 9) {
					if (Gdx.input.isKeyPressed(keypad((V[X])))) {
						PC += 2;
					}
				//Skip one instruction if key corresponding to VX is NOT
				// pressed.
				} else if (Y == 0xA) {
					if (!Gdx.input.isKeyPressed(keypad((V[X])))) {
						PC += 2;
					}
				}
				else {
					invalid = true;
				}
				break;
			case (0xF):
				switch (Y) {
					case 0:
						//Sets VX to the value of the delay timer
						if (N == 7) {
							V[X] = delay;
						//Halts operation until a key is pressed. Value
						// stored in VX.
						} else if (N == 0xA) {
/*
							for (int i = 0; i < 0x10; ++i) {
								if (Gdx.input.isKeyPressed(keypad((char)i))) {
									V[X] = (char)i;
									value = true;
									break;
								}
							}
*/
							if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
								V[X] = Input.Keys.NUM_1;
							} else if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
								V[X] = Input.Keys.NUM_2;
							} else if (Gdx.input.isKeyPressed(Input.Keys.NUM_3)) {
								V[X] = Input.Keys.NUM_3;
							} else if (Gdx.input.isKeyPressed(Input.Keys.NUM_4)) {
								V[X] = Input.Keys.C;
							} else if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
								V[X] = Input.Keys.NUM_4;
							} else if (Gdx.input.isKeyPressed(Input.Keys.W)) {
								V[X] = Input.Keys.NUM_5;
							} else if (Gdx.input.isKeyPressed(Input.Keys.E)) {
								V[X] = Input.Keys.NUM_6;
							} else if (Gdx.input.isKeyPressed(Input.Keys.R)) {
								V[X] = Input.Keys.D;
							} else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
								V[X] = Input.Keys.NUM_7;
							} else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
								V[X] = Input.Keys.NUM_8;
							} else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
								V[X] = Input.Keys.NUM_9;
							} else if (Gdx.input.isKeyPressed(Input.Keys.F)) {
								V[X] = Input.Keys.E;
							} else if (Gdx.input.isKeyPressed(Input.Keys.Z)) {
								V[X] = Input.Keys.A;
							} else if (Gdx.input.isKeyPressed(Input.Keys.X)) {
								V[X] = Input.Keys.NUM_0;
							} else if (Gdx.input.isKeyPressed(Input.Keys.C)) {
								V[X] = Input.Keys.B;
							} else if (Gdx.input.isKeyPressed(Input.Keys.V)) {
								V[X] = Input.Keys.F;
							} else {
								PC -= 2;
							}
						}
						break;
					case 1:
						//Sets delay timer to VX
						if (N == 5) {
							delay = V[X];
						//Sets sound timer to VX
						} else if (N == 8) {
							sound = V[X];
						//Sets I to I + VX. Does not trigger VF on COSMIC VIP.
						//TODO: Implement differences between Chip-8
						//TODO: interpreters in opcodes.
						} else if (N == 0xE) {
							if (I + V[X] > 0x0FFF) {
								V[0xF] = 1;
							}
							I += V[X];
						}
						else {
							invalid = true;
						}
						break;
					//Sets I to the address of the hexadecimal character
					// represented by the last nibble of VX.
					case 2:
						char character = (char) (V[X] >>> 4);
						switch (character) {
							case 0:
								I = 0x50;
								break;
							case 1:
								I = 0x55;
								break;
							case 2:
								I = 0x5A;
								break;
							case 3:
								I = 0x5F;
								break;
							case 4:
								I = 0x64;
								break;
							case 5:
								I = 0x69;
								break;
							case 6:
								I = 0x6E;
								break;
							case 7:
								I = 0x73;
								break;
							case 8:
								I = 0x78;
								break;
							case 9:
								I = 0x7D;
								break;
							case 0xA:
								I = 0x82;
								break;
							case 0xB:
								I = 0x87;
								break;
							case 0xC:
								I = 0x8C;
								break;
							case 0xD:
								I = 0x91;
								break;
							case 0xE:
								I = 0x96;
								break;
							case 0xF:
								I = 0x9B;
								break;
							default:
								invalid = true;
								break;
						}
						break;
					//Converts the value in VX to BCD and stores it in RAM at
					// address I, I+1, and I+2.
					case 3:
						char[] bcd = new char[3];
						bcd[0] = (char) (V[X] / 100);
						bcd[1] = (char) ((V[X] / 10) % 10);
						bcd[2] = (char) ((V[X] % 100) % 10);
						mem[I] = bcd[0];
						mem[I+1] = bcd[1];
						mem[I+2] = bcd[2];
						break;
					//Stores V0-X in RAM given by I. So V0 will be stored at
					// mem[I], V1 -> mem[I+1], .... ,etc.
					case 5:
						for (int i = 0; i <= X; ++i) {
							mem[I++] = V[i];
						}
						break;
					//Loads memory values from mem[I+X] in V0-X.
					case 6:
						for (int i = 0; i <= X; ++i) {
							V[i] = mem[I++];
						}
						break;
					default:
						invalid = true;
						break;
				}
				break;
			default:
				invalid = true;
				break;
		}
	}

	@Override
	public void render () {
		if (gui) {
			//TODO: gui window code
			ScreenUtils.clear(.9f, .9f, .9f, 1);

			screenViewport.apply();
			camera.update();

			stage.act();
			stage.draw();


			// Watching for user input
			if ((Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ^ Gdx
			.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) && Gdx.input.isKeyJustPressed(Input.Keys.O)) {
				// GUI File Chooser
				chooser.setSelectionMode(FileChooser.SelectionMode.FILES_AND_DIRECTORIES);
				chooser.setFavoriteFolderButtonVisible(true);

				TableUtils.setSpacingDefaults(chooser);

				chooser.setSize(Gdx.graphics.getWidth()/1.25f,
						Gdx.graphics.getHeight()/1.25f);
				chooser.setResizable(true);
				chooser.setFileTypeFilter(typeFilter);

				chooser.setListener(new SingleFileChooserListener() {
					@Override
					protected void selected(FileHandle file) {
						fileIn = file;
					}
				});

				stage.addActor(chooser);
			}
			if ((Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ^ Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) && Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
				System.exit(0);
			}

			if (fileIn != null) {
				gui = false;
				ScreenUtils.clear(0, 0, 0, 1);
				Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
			}
		} else {
			if (initial) {
				//Initialization
				initialize();
				initial = false;
			}

			int clockRate = 700;
			int fps = 60;
			int instructionsPerFrame = clockRate / fps;

			camera.setToOrtho(true, camera.viewportWidth,
					camera.viewportHeight);
			fitViewport.apply();
			camera.update();
			shape.setProjectionMatrix(camera.combined);

			//700 Hz TODO: make user adjustable
			for (int i = 0; i < instructionsPerFrame; ++i) {
				opcodeFetch();
				opcodeDecodeAndExecute();
				if (invalid) {
					System.out.println("Invalid opcode. Either ROM is corrupted, " +
							"or not a valid Chip-8 ROM.");
					System.exit(1);
				}
				if (draw) {
					//break;
					//ScreenUtils.clear(0, 0, 0, 1);

					draw();
				}
			}


/*
			if (draw) {
				//ScreenUtils.clear(0, 0, 0, 1);

				//camera.update();
				//shape.setProjectionMatrix(camera.combined);

				draw();
			}
*/


			//Decrement Timers, should run at 60 Hz, or 60 FPS
			delayTimer();
			soundTimer();

			//Watching for user input
			Graphics.DisplayMode displayMode = Gdx.graphics.getDisplayMode();
			if ((Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) ^ Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT)) && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
				if (!Gdx.graphics.isFullscreen()) {
					Gdx.graphics.setFullscreenMode(displayMode);
				} else {
					Gdx.graphics.setWindowedMode(800, 600);
				}
			}

			if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
				reset();
				Gdx.graphics.setWindowedMode(800, 600);
				camera.setToOrtho(false, camera.viewportWidth,
						camera.viewportHeight);
			}
		}
	}

	private void draw() {
		shape.begin(ShapeRenderer.ShapeType.Filled);
		for (int i = 0; i < display.length; ++i) {
			for (int j = 0; j < display[0].length; ++j) {
				if (display[i][j]) {
					shape.setColor(1, 1, 1, 1);
					shape.rect(i, j, 1, 1);
				} else if (!display[i][j]) {
					shape.setColor(0, 0, 0, 1);
					shape.rect(i, j, 1, 1);
				}
			}
		}
		shape.end();
		draw = false;
	}

	private void delayTimer() {
		if (delay == 0) {
			return;
		}
		--delay;
	}

	private void soundTimer() {
		if (sound == 0) {
			buzzer.stop();
			return;
		}
		// TODO: Buzz sound implement
		--sound;
		buzzer.play();
	}

	@Override
	public void dispose () {
		shape.dispose();
		buzzer.dispose();
		stage.dispose();
		VisUI.dispose();
	}

	@Override
	public void resize(int width, int height) {
		if (gui) {
			screenViewport.update(width, height);
			//Event resizeEvent = new Event();
			//for (Actor actor : stage.getActors()) {
				//actor.fire(resizeEvent);
			//}
			//ScreenUtils.clear(.9f, .9f, .9f, 1);
		} else {
			fitViewport.update(width, height);
			draw();
		}
		camera.position.set(camera.viewportWidth / 2,
				camera.viewportHeight / 2, 0);
	}

}
