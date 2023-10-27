import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Chippy {
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
	// Game
	private String filename;
	// Window
	private JFrame window;
	// Main Panel
	private ChippyPanel panel;

	public Chippy(String filename) {
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
		this.filename = filename;
		window = new JFrame();
		panel = new ChippyPanel();
		window.add(panel);
		window.pack();
		window.setSize(640, 480);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.getContentPane().setBackground(Color.BLACK);
		window.setLayout(null);
		window.setTitle("Chippy");
		window.setLocationRelativeTo(null);
		window.setVisible(true);
	}

	private char delayTimer() {
		if (delay == 0) {
			return 0x0;
		}
		return --delay;
	}

	private char soundTimer() {
		if (sound == 0) {
			return 0x0;
		}
		// Buzz sound implement
		return --sound;
	}

	private void graphics() {
	}

	private void font() {
		//0
		mem[0x50] = 0xF0;
		mem[0x51] = 0x90;
		mem[0x52] = 0x90;
		mem[0x53] = 0x90;
		mem[0x54] = 0xF0;
		//1
		mem[0x55] = 0x20;
		mem[0x56] = 0x60;
		mem[0x57] = 0x20;
		mem[0x58] = 0x20;
		mem[0x59] = 0x70;
		//2
		mem[0x5A] = 0xF0;
		mem[0x5B] = 0x10;
		mem[0x5C] = 0xF0;
		mem[0x5D] = 0x80;
		mem[0x5E] = 0xF0;
		//3
		mem[0x5F] = 0xF0;
		mem[0x60] = 0x10;
		mem[0x61] = 0xF0;
		mem[0x62] = 0x10;
		mem[0x63] = 0xF0;
		//4
		mem[0x64] = 0x90;
		mem[0x65] = 0x90;
		mem[0x66] = 0xF0;
		mem[0x67] = 0x10;
		mem[0x68] = 0x10;
		//5
		mem[0x69] = 0xF0;
		mem[0x6A] = 0x80;
		mem[0x6B] = 0xF0;
		mem[0x6C] = 0x10;
		mem[0x6D] = 0xF0;
		//6
		mem[0x6E] = 0xF0;
		mem[0x6F] = 0x80;
		mem[0x70] = 0xF0;
		mem[0x71] = 0x90;
		mem[0x72] = 0xF0;
		//7
		mem[0x73] = 0xF0;
		mem[0x74] = 0x10;
		mem[0x75] = 0x20;
		mem[0x76] = 0x40;
		mem[0x77] = 0x40;
		//8
		mem[0x78] = 0xF0;
		mem[0x79] = 0x90;
		mem[0x7A] = 0xF0;
		mem[0x7B] = 0x90;
		mem[0x7C] = 0xF0;
		//9
		mem[0x7D] = 0xF0;
		mem[0x7E] = 0x90;
		mem[0x7F] = 0xF0;
		mem[0x80] = 0x10;
		mem[0x81] = 0xF0;
		//A
		mem[0x82] = 0xF0;
		mem[0x83] = 0x90;
		mem[0x84] = 0xF0;
		mem[0x85] = 0x90;
		mem[0x86] = 0x90;
		//B
		mem[0x87] = 0xE0;
		mem[0x88] = 0x90;
		mem[0x89] = 0xE0;
		mem[0x8A] = 0x90;
		mem[0x8B] = 0xE0;
		//C
		mem[0x8C] = 0xF0;
		mem[0x8D] = 0x80;
		mem[0x8E] = 0x80;
		mem[0x8F] = 0x80;
		mem[0x90] = 0xF0;
		//D
		mem[0x91] = 0xE0;
		mem[0x92] = 0x90;
		mem[0x93] = 0x90;
		mem[0x94] = 0x90;
		mem[0x95] = 0xE0;
		//E
		mem[0x96] = 0xF0;
		mem[0x97] = 0x80;
		mem[0x98] = 0xF0;
		mem[0x99] = 0x80;
		mem[0x9A] = 0xF0;
		//F
		mem[0x9B] = 0xF0;
		mem[0x9C] = 0x80;
		mem[0x9D] = 0xF0;
		mem[0x9E] = 0x80;
		mem[0x9F] = 0x80;
	}

	private void game() throws IOException {
		int ch;
		int i = 0x200;
		FileInputStream fileIn = new FileInputStream(filename);
		while ((ch = fileIn.read()) != -1) {
			mem[i++] = (char) ch;
		}
		fileIn.close();
	}

	private void memDump() throws IOException {
		//Memory dump
		FileOutputStream fileOut = new FileOutputStream("mem.ch8");
		for (int j = 0; j < mem.length; ++j) {
			fileOut.write(mem[j]);
		}
		fileOut.close();
	}

	private void initialize() throws IOException {
		//Initialization process
		//Initialize font into memory
		font();
		//Placing game into memory
		game();
		//Setting PC to proper memory location
		PC = 0x200;
		//Setting SP to proper stack location
		SP = stack[15];
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
					char divisor = 0x0080;
					for (int k = 8; k > 0; --k) {
						char pixel = (char) ((sprite & divisor) >>> k - 1);
						divisor /= 2;
						if (pixel == 1 && display[xCoord][yCoord]) {
							display[xCoord][yCoord] = false;
							panel.setColor(Color.BLACK);
							window.repaint();
							V[0xF] = 1;
						}
						if (pixel == 1 && !display[xCoord][yCoord]) {
							display[xCoord][yCoord] = true;
							panel.setColor(Color.WHITE);
							window.repaint();
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

	public static void main(String[] args) {
		//System.out.println("Hello World");
		if (args.length != 1) {
			System.err.println("How to use program: 'java Chippy [ROM]");
			System.exit(1);
		}
		Chippy chip = new Chippy(args[0]);

		//Initialization
		try {
			chip.initialize();
		} catch (IOException e) {
			System.err.println("Error, file not found.");
			System.exit(1);
		}

		//Main loop
		while (true) {
			//Fetch
			chip.opcodeFetch();
			//Decode and Execute
			chip.opcodeDecodeAndExecute();
		}
	}
	private class ChippyPanel extends JPanel {
		private Color color;
		public ChippyPanel() {
			color = Color.BLACK;
		}
		public void setColor(Color color) {
			this.color = color;
			repaint();
		}
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(color);
			g.drawLine(xCoord, yCoord, xCoord, yCoord);
		}
	}
}