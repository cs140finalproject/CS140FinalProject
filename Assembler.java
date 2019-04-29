package project;

import java.util.Set;

public interface Assembler {
	Set<String> noArgument = Set.of("HALT", "NOP", "NOT");
	
	int assemble(String inputFileName, String outputFileName, StringBuilder error);
	
	class DataPair{
		protected int address;
		protected int value;
		
		public DataPair(int address, int value) {
			this.address = address;
			this.value = value;
		}
		
		public String toString() {
			return "DataPair (" + address + ", " + value + ")";
		}
	}
}
