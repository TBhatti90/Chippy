import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.*;

public class Chippy {
	private static char delayTimer(char delay) {
		if (delay == 0) {
			return 0x0;
		}
		return --delay;
	}
	private static char soundTimer(char sound) {
		if (sound == 0) {
			return 0x0;
		}
		// Buzz sound implement
		return --sound;
	}
	public static void graphics(boolean[][] display) {
		JFrame frame = new JFrame();
		frame.setSize(display.length*7,display[0].length*7);
		frame.setLayout(null);
		frame.setVisible(true);
	}
	private static void font(char[] mem) {
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
	public static void main(String[] args) throws IOException {
		//System.out.println("Hello World");
		if (args.length > 1) {
			throw new IndexOutOfBoundsException("Too many/little arguments, " +
					"please specify 1.");
		}
		if (args.length == 0) {
			System.out.printf("How to use program: java Chippy [ROM]\n");
			return;
		}
		FileInputStream fileIn = new FileInputStream(args[0]);
		//FileOutputStream fileOut = new FileOutputStream("mem.ch8");

		// RAM
		char[] mem = new char[4096];
		// Registers
		char[] V = new char[16];
		// Stores addresses, only lowest 12 bits are used
		char I = 0;
		// Delay Register
		char delay = 0;
		// Sound Register
		char sound = 0;
		// Stack
		char[] stack = new char[16];
		// Program Counter, stores currently executing address
		char PC = 0;
		// Stack pointer, points to top of stack
		char SP = 0;
		// Display
		boolean[][] display = new boolean[64][32];
		// Instruction
		char instruction = 0;
		//Keypad
		char[] keypad = new char[16];

		//Initialization process
		//Initialize font into memory
		font(mem);
		//Initialize graphics
		graphics(display);
		//Placing game into memory
		int ch;
		int i = 0x200;
		while ((ch = fileIn.read()) != -1) {
			mem[i++] = (char)ch;
		}
		fileIn.close();
		//Setting PC to proper memory location
		PC = 0x200;
		//Setting SP to proper stack location
		SP = stack[15];

		//Memory dump
		//for (int j = 0; j < mem.length; ++j) {
			//fileOut.write(mem[j]);
		//}
		//fileOut.close();

		//Main loop
		while (true) {
			//Fetch
			char byte1 = mem[PC];
			char byte2 = mem[PC+1];
			byte1 = (char) (byte1 << 8);
			instruction = (char)(byte1 | byte2);
			PC = (char)(PC+2);
			//Decode
			//Extract bytes from instruction.
			char nibble = (char)((instruction&0xF000)>>>12);
			char X = (char)((instruction&0x0F00)>>>8);
			char Y = (char)((instruction&0x00F0)>>>4);
			char N = (char)(instruction&0x000F);
			char NN = (char)(instruction&0x00FF);
			char NNN = (char)(instruction&0x0FFF);
			//Decode and Execute
			switch(nibble) {
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
				case(1):
					PC = NNN;
					break;
				case(2):
					break;
				case(3):
					break;
				case(4):
					break;
				case(5):
					break;
				//Sets VX to NN
				case(6):
					V[X] = NN;
					break;
				//Adds NN to VX. (Carry flag is not changed).
				case(7):
					V[X] += NN;
					break;
				case(8):
					break;
				case(9):
					break;
				//Sets I to NNN
				case(0xA):
					I = NNN;
					break;
				case(0xB):
					break;
				case(0xC):
					break;
				//Draws a sprite at the coordinates (VX,VY), with a width of
				// 8 pixels and a height of N pixels.
				case(0xD):
					char xCoord = (char)(V[X] % display.length);
					char yCoord = (char)(V[Y] % display[0].length);
					V[0xF] = 0;
					for (int j = 0; j < N; ++j) {
						display[xCoord][yCoord] = false;
						char sprite = mem[I+j];
					}
					break;
				case(0xE):
					break;
				case(0xF):
					break;
				default:
					break;
			}
		}
	}
}
