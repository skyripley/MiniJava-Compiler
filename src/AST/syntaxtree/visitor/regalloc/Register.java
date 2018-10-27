package AST.syntaxtree.visitor.regalloc;

public class Register extends Value
{		
	public Register(int registerIndex)
	{		
		super(registerIndex);
	}
	
	public int getRegisterIndex()
	{
		return getValue();
	}
}
