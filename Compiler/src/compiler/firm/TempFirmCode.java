package compiler.firm;

import firm.ClassType;
import firm.Construction;
import firm.Entity;
import firm.Graph;
import firm.MethodType;
import firm.Mode;
import firm.PointerType;
import firm.PrimitiveType;
import firm.Program;
import firm.Type;
import firm.nodes.Load;
import firm.nodes.Node;
import firm.nodes.Store;

/**
 * temporary code for firm graph
 *
 * to be deleted
 */
public class TempFirmCode {

	/**
	 * create empty method
	 *
	 * static void empty(){};
	 *
	 */
	public static void createStaticEmptyMethod()
	{
		// create method empty in global scope
		MethodType mainType = new MethodType(new Type[] {}, new Type[] {});
		Entity topEntity = new Entity(Program.getGlobalType(), "empty(V)V", mainType);

		// create graph for method empty
		Graph graph = new Graph(topEntity, 0);
		Construction construction = new Construction(graph);

		// ------------------------- method body --------------------------------------------

		// ------------------------- method return statement --------------------------------

		Node returnNode = construction.newReturn(construction.getCurrentMem(), new Node[] {});
		graph.getEndBlock().addPred(returnNode);

		// ------------------------- method body end -----------------------------------------

		construction.setUnreachable();
		construction.finish();
	}

	/**
	 * params and return type
	 *
	 * static int mparam(int x){ return x; };
	 *
	 */
	public static void createStaticMethodWithParam()
	{
		Mode modeInteger = Mode.getIs();
		PrimitiveType intType = new PrimitiveType(modeInteger);

		MethodType mainType = new MethodType(new Type[] { intType }, new Type[] { intType });
		Entity topEntity = new Entity(Program.getGlobalType(), "mparam(I)I", mainType);

		int varX = 0;
		int varCount = 1;
		Graph graph = new Graph(topEntity, varCount);
		Construction construction = new Construction(graph);

		// get parameter and save it in variable
		Node args = graph.getArgs();
		Node projX = construction.newProj(args, modeInteger, 0 /* parameter number 0 */);
		construction.setVariable(varX, projX);

		// get variable and return it
		Node varXNode = construction.getVariable(varX, modeInteger);
		Node returnNode = construction.newReturn(construction.getCurrentMem(), new Node[] { varXNode });
		graph.getEndBlock().addPred(returnNode);

		construction.setUnreachable();
		construction.finish();
	}

	/**
	 * class A{ Test y;
	 *
	 * void method([A this,] int cntr){ int x = 1; this.y.z += cntr + x; }; }
	 * 
	 * class Test{ int z;}
	 */
	public static void createMethodWithReferenceToClass() {
		// define class
		ClassType classA = new ClassType("A");
		ClassType classTest = new ClassType("Test");

		Mode modeInt = Mode.getIs();
		Mode modeRef = Mode.getP();
		Type intType = new PrimitiveType(modeInt);
		Type reference_to_Test = new PointerType(classTest);

		// define field y
		Entity A__Y = new Entity(classA, "y", classTest);

		// define field z
		Entity A__Z = new Entity(classTest, "z", intType);

		// define method "method"
		MethodType A__method_Type = new MethodType(new Type[] { reference_to_Test, intType }, new Type[] {});
		Entity A__method = new Entity(classA, "method(A,I)V with test", A__method_Type);

		// create graph for entity A::method
		int varThisNum = 0;
		int varThisYNum = varThisNum + 1;
		int varCntrNum = varThisYNum + 1;
		int varLocalXNum = varCntrNum + 1;
		int varCount = varLocalXNum + 1;
		Graph graph = new Graph(A__method, varCount);
		Construction construction = new Construction(graph);

		// ------------------------- method body --------------------------------------------

		// load this and counter in variable
		Node args = graph.getArgs();
		Node projThis = construction.newProj(args, modeRef, 0);
		Node projCntr = construction.newProj(args, modeInt, 1);
		construction.setVariable(varThisNum, projThis);
		construction.setVariable(varCntrNum, projCntr);
		Node c1 = construction.newConst(1, modeInt);
		construction.setVariable(varLocalXNum, c1);

		// add x + cntr = tmp1
		Node varX = construction.getVariable(varLocalXNum, modeInt);
		Node argCntr = construction.getVariable(varCntrNum, modeInt);
		Node addExpr = construction.newAdd(varX, argCntr, modeInt);

		// load this.y and add this.y to tmp1
		Node mem = construction.getCurrentMem();
		// Node mem = construction.newDummy(Mode.getM());
		Node addrof_thisY = construction.newMember(projThis, A__Y);
		Node loadThisY = construction.newLoad(mem, addrof_thisY, modeRef);
		Node loadThisYResult = construction.newProj(loadThisY, modeRef, Load.pnRes);
		// load this.y.z and add this.y.z to tmp1
		Node addrof_Z = construction.newMember(loadThisYResult, A__Z);
		Node loadThisYZ = construction.newLoad(mem, addrof_Z, modeInt);
		Node loadThisYZResult = construction.newProj(loadThisYZ, modeInt, Load.pnRes);

		Node loadMem = construction.newProj(loadThisYZ, Mode.getM(), Load.pnM);
		construction.setCurrentMem(loadMem);
		mem = construction.getCurrentMem();

		Node addExpr2 = construction.newAdd(loadThisYZResult, addExpr, modeInt);

		// store this.y
		Node storeThisY = construction.newStore(mem, construction.getVariable(varThisNum, modeRef), addExpr2);
		Node storeMem = construction.newProj(storeThisY, Mode.getM(), Store.pnM);

		// ------------------------- method return statement --------------------------------

		Node returnNode = construction.newReturn(construction.getCurrentMem(), new Node[] {});
		returnNode.setPred(0, storeMem);
		graph.getEndBlock().addPred(returnNode);

		construction.setUnreachable();
		construction.finish();
	}

	/**
	 * class A{ int y;
	 *
	 * void method([A this,] int cntr){ int x = 1; this.y += cntr + x; }; }
	 */
	public static void createMethodWithLocalVar()
	{
		// define class
		ClassType classA = new ClassType("A");

		Mode modeInt = Mode.getIs();
		Mode modeRef = Mode.getP();
		Type intType = new PrimitiveType(modeInt);
		Type reference_to_A = new PointerType(classA);

		// define field y
		Entity A__y = new Entity(classA, "y", intType);

		// define method "method"
		MethodType A__method_Type = new MethodType(new Type[] { reference_to_A, intType }, new Type[] {});
		Entity A__method = new Entity(classA, "method(A,I)V", A__method_Type);

		// create graph for entity A::method
		int varThisNum = 0;
		int varThisYNum = varThisNum + 1;
		int varCntrNum = varThisYNum + 1;
		int varLocalXNum = varCntrNum + 1;
		int varCount = varLocalXNum + 1;
		Graph graph = new Graph(A__method, varCount);
		Construction construction = new Construction(graph);

		// ------------------------- method body --------------------------------------------

		// load this and counter in variable
		Node args = graph.getArgs();
		Node projThis = construction.newProj(args, modeRef, 0);
		Node projCntr = construction.newProj(args, modeInt, 1);
		construction.setVariable(varThisNum, projThis);
		construction.setVariable(varCntrNum, projCntr);
		Node c1 = construction.newConst(1, modeInt);
		construction.setVariable(varLocalXNum, c1);

		// add x + cntr = tmp1
		Node varX = construction.getVariable(varLocalXNum, modeInt);
		Node argCntr = construction.getVariable(varCntrNum, modeInt);
		Node addExpr = construction.newAdd(varX, argCntr, modeInt);

		// load this.y and add this.y to tmp1
		Node mem = construction.getCurrentMem();
		// Node mem = construction.newDummy(Mode.getM());
		Node addrof_thisY = construction.newMember(projThis, A__y);
		Node loadThisY = construction.newLoad(mem, addrof_thisY, modeInt);
		Node loadThisYResult = construction.newProj(loadThisY, modeInt, Load.pnRes);
		Node loadMem = construction.newProj(loadThisY, Mode.getM(), Load.pnM);
		construction.setCurrentMem(loadMem);
		mem = construction.getCurrentMem();

		Node addExpr2 = construction.newAdd(loadThisYResult, addExpr, modeInt);

		// store this.y
		Node storeThisY = construction.newStore(mem, construction.getVariable(varThisNum, modeRef), addExpr2);
		Node storeMem = construction.newProj(storeThisY, Mode.getM(), Store.pnM);

		// ------------------------- method return statement --------------------------------

		Node returnNode = construction.newReturn(construction.getCurrentMem(), new Node[] {});
		returnNode.setPred(0, storeMem);
		graph.getEndBlock().addPred(returnNode);

		construction.setUnreachable();
		construction.finish();
	}

	public static void createPrintIntGraph()
	{
		Mode modeInt = Mode.getIs();

		MethodType mainType = new MethodType(new Type[] {}, new Type[] {});
		Entity topEntity = new Entity(Program.getGlobalType(), "print_int_main(V)V", mainType);

		int varXNum = 0;
		int varCount = 1;
		Graph graph = new Graph(topEntity, varCount);
		Construction construction = new Construction(graph);

		// create entity print_int;
		MethodType print_int_type = new MethodType(new Type[] { new PrimitiveType(modeInt) }, new Type[] {});
		Entity print_int = new Entity(Program.getGlobalType(), "print_int(I)V", print_int_type);
		Node ptr = construction.newAddress(print_int);

		/*
		 * print_int(42);
		 */
		Node c42 = construction.newConst(42, modeInt);

		Node mem = construction.getCurrentMem();
		Node callPrintInt = construction.newCall(mem, ptr, new Node[] { c42 }, print_int_type);
		Node projCall = construction.newProj(callPrintInt, Mode.getM(), 0 /* what is it */);
		construction.setCurrentMem(projCall); /* todo do we need this? */

		/*
		 * int x = 42; print_int(x);
		 */
		construction.setVariable(varXNum, c42);
		Node varX = construction.getVariable(varXNum, modeInt);
		mem = construction.getCurrentMem();
		Node callPrintInt2 = construction.newCall(mem, ptr, new Node[] { varX }, print_int_type);
		Node projCall2 = construction.newProj(callPrintInt2, Mode.getM(), 0 /* what is it */);
		construction.setCurrentMem(projCall2); /* todo do we need this? */

		Node returnNode = construction.newReturn(construction.getCurrentMem(), new Node[] {});
		returnNode.setPred(0, projCall2);
		graph.getEndBlock().addPred(returnNode);

		construction.setUnreachable();
		construction.finish();
	}

}
