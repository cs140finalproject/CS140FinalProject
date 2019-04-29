package project;

import static project.Instruction.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public class Machine implements HaltCallback {
	public final Map<Integer, Consumer<Instruction>> ACTION = new TreeMap<>();
	private CPU cpu = new CPU();
	private Memory memory = new Memory();
	private boolean withGUI = false;
	private HaltCallback callBack;

	//machine constructor
	public Machine(HaltCallback cb) {
		callBack = cb;
		
		//ACTION entry for "NOP"
		ACTION.put(OPCODES.get("NOP"), instr -> {
			int flags = instr.opcode & 6; // remove parity bit that will have been verified
			if(flags != 0){
				String fString = "(" + (flags%8 > 3?"1":"0") + (flags%4 > 1?"1":"0") + ")";
				throw new IllegalInstructionException("Illegal flags for this instruction: " + fString);
			}
			cpu.pc++;			
		});
		// ACTION entry for "HALT"
		ACTION.put(OPCODES.get("HALT"), instr -> {
			int flags = instr.opcode & 6;
			if(flags != 0) {
				String fString = "(" + (flags%8 > 3?"1":"0") + (flags%4 > 1?"1":"0") + ")";
				throw new IllegalInstructionException("Illegal flags for this instruction: " + fString);
			}
			halt();
		});
		// ACTION entry for "JUMP"
		ACTION.put(OPCODES.get("JUMP"), instr -> {
			int flags = instr.opcode & 6;
			if(flags == 0) {
				cpu.pc = cpu.pc + instr.arg;
			}
			else if(flags == 2) {
				//FIXME: "puts arg in cpu.pc"
				cpu.pc = instr.arg;
			}
			else if(flags == 4) {
				cpu.pc += memory.getData(instr.arg);
			}
			else {
				cpu.pc = memory.getData(instr.arg);
			}
		});
		
		// ACTION entry for "JMPZ"
		ACTION.put(OPCODES.get("JMPZ"), instr -> {
			int flags = instr.opcode & 6;
			if(cpu.accum == 0) {
				if(flags == 0) {
					cpu.pc = cpu.pc + instr.arg;
				}
				else if(flags == 2) {
					//FIXME: "puts arg in cpu.pc"
					cpu.pc = instr.arg;
				}
				else if(flags == 4) {
					cpu.pc += memory.getData(instr.arg);
				}
				else {
					cpu.pc = memory.getData(instr.arg);
				}
			}
			else {
				cpu.pc++;
			}

		});
		// ACTION entry for "LOD"
		ACTION.put(OPCODES.get("LOD"), instr -> {
			int flags = instr.opcode & 6;
			if(flags == 0) {
				cpu.accum += memory.getData(instr.arg);
			}
			else if(flags == 2) {
				cpu.accum = instr.arg;
			}
			else if(flags == 4) {
				cpu.accum += memory.getData(memory.getData(instr.arg));
			}
			else {
				String fString = "(" + (flags%8 > 3?"1":"0") + (flags%4 > 1?"1":"0") + ")";
				throw new IllegalInstructionException("Illegal flags for this instruction: " + fString);
			}
		});
		// ACTION entry for "STO"
		ACTION.put(OPCODES.get("STO"), instr -> {
			int flags = instr.opcode & 6;
			if (flags == 0) // Direct Addressing
				memory.setData(instr.arg, cpu.accum);
			else if (flags == 4) // Indirect Addressing
				memory.setData(memory.getData(instr.arg), cpu.accum);
			else // Illegal flag
			{
				String fString = "(" + (flags%8 > 3?"1":"0") + (flags%4 > 1?"1":"0") + ")";
				throw new IllegalInstructionException("Illegal flags for this instruction: " + fString);
			}
			cpu.pc++;
		});

		// ACTION entry for "NOT"
		ACTION.put(OPCODES.get("NOT"), instr -> {
			int flags = instr.opcode & 6;
			if(flags != 0) {
				String fString = "(" + (flags%8 > 3?"1":"0") + (flags%4 > 1?"1":"0") + ")";
				throw new IllegalInstructionException("Illegal flags for this instruction: " + fString);
			}
			else {
				if(cpu.accum == 0) {
					cpu.accum = 1;
				}
				else {
					cpu.accum = 0;
				}
			}
		});
		// ACTION entry for "AND"
		ACTION.put(OPCODES.get("AND"), instr -> {
			int flags = instr.opcode & 6;
			flags = flags & 0x6;

			if (flags == 0) // Direct Addressing
			{
				if (cpu.accum != 0 && memory.getData(instr.arg) != 0)
					cpu.accum = 1;
				else
					cpu.accum = 0;
			}
			else if (flags == 2) // Immediate Addressing
			{
				if (cpu.accum != 0 && instr.arg != 0)
					cpu.accum = 1;
				else
					cpu.accum = 0;
			}
			else // Illegal Flags
			{
				String fString = "(" + (flags%8 > 3?"1":"0") + (flags%4 > 1?"1":"0") + ")";
				throw new IllegalInstructionException("Illegal flags for this instruction: " + fString);
			}
			cpu.pc++;
		});

		// ACTION entry for "CMPL"
		ACTION.put(OPCODES.get("CMPL"), instr -> {
			int flags = instr.opcode & 6;
			if(flags != 0) {
				String fString = "(" + (flags%8 > 3?"1":"0") + (flags%4 > 1?"1":"0") + ")";
				throw new IllegalInstructionException("Illegal flags for this instruction: " + fString);
			}
			else {
				if(memory.getData(instr.arg) < 0){
					cpu.accum = 1;
				}
				else {
					cpu.accum = 0;
				}
			}
			cpu.pc++;
		});
		// ACTION entry for "CMPZ"
		ACTION.put(OPCODES.get("CMPZ"), instr ->  {
			int flags = instr.opcode & 6;
			if(flags != 0) {
				String fString = "(" + (flags%8 > 3?"1":"0") + (flags%4 > 1?"1":"0") + ")";
				throw new IllegalInstructionException("Illegal flags for this instruction: " + fString);
			}
			else {
				if(memory.getData(instr.arg) == 0) {
					cpu.accum = 1;
				}
				else {
					cpu.accum = 0;
				}

			}
			cpu.pc++;
		});
		// ACTION entry for "ADD"
		ACTION.put(OPCODES.get("ADD"), instr -> {
			int flags = instr.opcode & 6; // remove parity bit that will have been verified
			if(flags == 0) { // direct addressing
				cpu.accum += memory.getData(instr.arg);
			} else if(flags == 2) { // immediate addressing
				cpu.accum += instr.arg;
			} else if(flags == 4) { // indirect addressing
				cpu.accum += memory.getData(memory.getData(instr.arg));				
			} else {
				String fString = "(" + (flags%8 > 3?"1":"0") 
						+ (flags%4 > 1?"1":"0") + ")";
				throw new IllegalInstructionException("Illegal flags for this instruction: " + fString);
			}
			cpu.pc++;			
		});
		// ACTION entry for "SUB"
		ACTION.put(OPCODES.get("SUB"), instr -> {
			int flags = instr.opcode & 6; // remove parity bit that will have been verified
			if(flags == 0) { // direct addressing
				cpu.accum -= memory.getData(instr.arg);
			} else if(flags == 2) { // immediate addressing
				cpu.accum -= instr.arg;
			} else if(flags == 4) { // indirect addressing
				cpu.accum -= memory.getData(memory.getData(instr.arg));				
			} else {
				String fString = "(" + (flags%8 > 3?"1":"0") 
						+ (flags%4 > 1?"1":"0") + ")";
				throw new IllegalInstructionException("Illegal flags for this instruction: " + fString);
			}
			cpu.pc++;			
		});

		// ACTION entry for "MUL"
		ACTION.put(OPCODES.get("MUL"), instr -> {
			int flags = instr.opcode & 6; // remove parity bit that will have been verified
			if(flags == 0) { // direct addressing
				cpu.accum *= memory.getData(instr.arg);
			} else if(flags == 2) { // immediate addressing
				cpu.accum *= instr.arg;
			} else if(flags == 4) { // indirect addressing
				cpu.accum *= memory.getData(memory.getData(instr.arg));				
			} else {
				String fString = "(" + (flags%8 > 3?"1":"0") 
						+ (flags%4 > 1?"1":"0") + ")";
				throw new IllegalInstructionException("Illegal flags for this instruction: " + fString);
			}
			cpu.pc++;			
		});

		// ACTION code for "DIV" - throw error when div by 0
		ACTION.put(OPCODES.get("DIV"), instr -> {
			int flags = instr.opcode & 6; // remove parity bit that will have been verified
			if (flags == 0)  // direct addressing
			{
				if (memory.getData(instr.arg) == 0)
					throw new DivideByZeroException("Bad input");
				cpu.accum /= memory.getData(instr.arg);
			}
			else if (flags == 2) // immediate addressing
			{ 
				if (instr.arg == 0)
					throw new DivideByZeroException("Bad input");
				cpu.accum /= instr.arg;
			}
			else if (flags == 4)  // indirect addressing
			{
				if (memory.getData(memory.getData(instr.arg)) == 0)
					throw new DivideByZeroException("Bad input");
				cpu.accum /= memory.getData(memory.getData(instr.arg));				
			}
			else  // here the illegal case is "11"
			{
				String fString = "(" + (flags%8 > 3?"1":"0") + (flags%4 > 1?"1":"0") + ")";
				throw new IllegalInstructionException("Illegal flags for this instruction: " + fString);
			}
			cpu.pc++;	
		});
	}

	public int[] getData() {
		return memory.getData();
	}
	public int getData(int i){
		return memory.getData(i);
	}
	public int[] getData(int i, int j){
		return memory.getData(i, j);
	}
	public int getPC() {
		return cpu.pc;
	}
	public int getAccum() {
		return cpu.accum;
	}
	public void setData(int i, int j) {
		memory.setData(i, j);		
	}
	public void setAccum(int i) {
		cpu.accum = i;
	}
	public void setPC(int i) {
		cpu.pc = i;
	}

	@Override
	public void halt() {
		callBack.halt();
	}
	
	public Instruction getCode(int index) {
		return memory.getCode(index);
	}
	public int getProgramSize() {
		return memory.getProgramSize();
	}
	public void addCode(Instruction j) {
		memory.addCode(j);	
	}
	// package private
	void setCode(int index, Instruction instr) {
		memory.setCode(index, instr);	
	}
	public List<Instruction> getCode() {
		return memory.getCode();
	}
	
	//package private
	Instruction[] getCode(int min, int max) {
		return memory.getCode(min,max);
	}
	
	public int getChangedDataIndex() {
		return memory.getChangedDataIndex();
	}
	
	public void clear() {
		memory.clearData();
		memory.clearCode();
		cpu.pc = 0;
		cpu.accum = 0;
	}
	
	public void step() {
		try {
			Instruction instr = getCode(cpu.pc);
			Instruction.checkParity(instr);
			ACTION.get(instr.opcode/8).accept(instr);
		}
		catch(Exception e) {
		e.printStackTrace();
		halt();
		throw e;
	}
	}

	private class CPU{
		private int accum;
		private int pc;
	}
}
