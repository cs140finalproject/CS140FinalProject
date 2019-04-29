package project;

public class Test {
	public static void main(String[] args) {
		Instruction instr = new Instruction((byte) 10101010, 3);
		Instruction.checkParity(instr);
	}
	}
