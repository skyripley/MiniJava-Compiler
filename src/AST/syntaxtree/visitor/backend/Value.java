package AST.syntaxtree.visitor.backend;

public abstract class Value
{
	protected String str;
	
	public Value(String str)
	{
		this.str = str;
	}
	
	@Override
	public String toString()
	{
		return str;
	}
}
