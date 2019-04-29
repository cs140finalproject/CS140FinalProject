package project;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class Memory {
	public static final int DATA_SIZE = 512;
	private int[] data =  new int[DATA_SIZE];
	int CODE_SIZE = 256;
	List<Instruction> code = new ArrayList<>();
	int changedDataIndex = -1;

	// how to make a method package private
	// below: supposed to be package private

	public int[] getData(int min, int max) {
		return Arrays.copyOfRange(data, min, max);
	}

	public int[] getData() {
		return data;
	}

	public int getData(int index) {
		if (index < 0 || index > DATA_SIZE)
			throw new CodeAccessException("Attempt to get data outside its bounds");
		return data[index];
	}

	public void setData(int index, int value) {
		if (index < 0 || index > DATA_SIZE)
			throw new CodeAccessException("Attempt to set data outside its bounds");
		data[index] = value;
		changedDataIndex = index;
	}

	public void clearData() {
		for(int i = 0; i < DATA_SIZE; i++) {
			data[i] = 0;
		}
		changedDataIndex = -1;
	}

	public Instruction getCode(int index) {
		if(0 <= index && index < code.size()) {
			return code.get(index);
		}
		throw new CodeAccessException("Illegal access to code");
	}

	public List<Instruction> getCode() {
		return code;
	}

	public Instruction[] getCode(int min, int max) {
		// throw CodeAccessException if it is NOT true 
		// that 0 <= min <= max < code.size()
		Instruction[] temp = {};
		temp = code.toArray(temp);
		return Arrays.copyOfRange(temp, min, max); 
	}

	void addCode(Instruction value) {
		if(code.size() < CODE_SIZE) {
			code.add(value);
		}
	}

	void setCode(int index, Instruction instr) {
		if(0 <= index && index < code.size()) {
			code.set(index, instr);
		}
		else {
			throw new CodeAccessException("Illegal access to code");
		}
	}

	void clearCode() {
		code.clear();
	}

	int getProgramSize() {
		return code.size();
	}
	
	int getChangedDataIndex() {
		return changedDataIndex;
	}
}
