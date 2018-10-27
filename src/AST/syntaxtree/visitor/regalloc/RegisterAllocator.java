package AST.syntaxtree.visitor.regalloc;

import AST.syntaxtree.visitor.ops.FunctionDeclaration;

import java.util.Map;

public interface RegisterAllocator
{		
	Map<String, Value> allocateRegisters(FunctionDeclaration func, int k);
	int getStackSize();
	int getSpillCount();
	int getNumRegistersUsed();	
}
