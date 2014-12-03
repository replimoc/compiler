package compiler.firm;

import firm.ArrayType;
import firm.ClassType;
import firm.Construction;
import firm.Entity;
import firm.Graph;
import firm.MethodType;
import firm.Mode;
import firm.Mode.Arithmetic;
import firm.PointerType;
import firm.PrimitiveType;
import firm.Program;
import firm.Relation;
import firm.Type;
import firm.nodes.Block;
import firm.nodes.Call;
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
		Mode modeRef = Mode.createReferenceMode("P64", Arithmetic.TwosComplement, 64, 64);
		Mode.setDefaultModeP(modeRef);
		Type intType = new PrimitiveType(modeInt);
		Type reference_to_A = new PointerType(classA);

		// define field y
		Entity A__Y = new Entity(classA, "y", classTest);

		// define field z
		Entity TEST_Z = new Entity(classTest, "z", intType);

		// define method "method"
		MethodType A__method_Type = new MethodType(new Type[] { reference_to_A, intType }, new Type[] {});
		Entity A__method = new Entity(classA, "methodWithIndirection(A,I)V", A__method_Type);

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
		Node addrof_Z = construction.newMember(loadThisYResult, TEST_Z);
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
	 * class A{
	 *
	 * int method([A this,] int cntr){ if(5 == 3) {cntr = 6;} else {cntr = 10;}return cntr;} }
	 * 
	 */
	public static void createMethodWithComparison() {
		// define class
		ClassType classA = new ClassType("A");

		Mode modeInt = Mode.getIs();
		Mode modeRef = Mode.createReferenceMode("P64", Arithmetic.TwosComplement, 64, 64);
		Mode.setDefaultModeP(modeRef);
		Type intType = new PrimitiveType(modeInt);
		PointerType reference_to_A = new PointerType(classA);

		// define method "method"
		MethodType A__method_Type = new MethodType(new Type[] { reference_to_A, intType }, new Type[] { intType });
		Entity A__method = new Entity(classA, "methodWithComparison(A,I)I", A__method_Type);

		// create graph for entity A::method
		int varThisNum = 0;
		int varCntrNum = varThisNum + 1;
		int varCount = varCntrNum + 1;

		Graph graph = new Graph(A__method, varCount);
		Construction construction = new Construction(graph);

		// ------------------------- method body --------------------------------------------

		// load this and counter in variable
		Node args = graph.getArgs();
		Node projThis = construction.newProj(args, modeRef, 0);
		Node projCntr = construction.newProj(args, modeInt, 1);
		construction.setVariable(varThisNum, projThis);
		construction.setVariable(varCntrNum, projCntr);

		// if (5 == 3)
		Node const3 = construction.newConst(3, modeInt);
		Node const5 = construction.newConst(5, modeInt);
		Node equals = construction.newCmp(const5, const3, Relation.Equal);
		Node cond = construction.newCond(equals);
		Node condTrue = construction.newProj(cond, Mode.getX(), 1);
		Node condFalse = construction.newProj(cond, Mode.getX(), 0);
		construction.getCurrentBlock().mature();

		// true block
		Block trueBlock = construction.newBlock();
		trueBlock.addPred(condTrue);
		trueBlock.mature();
		construction.setCurrentBlock(trueBlock);
		Node const6 = construction.newConst(6, modeInt);
		construction.setVariable(varCntrNum, const6);
		Node trueJmp = construction.newJmp();

		// false block
		Block falseBlock = construction.newBlock();
		falseBlock.addPred(condFalse);
		falseBlock.mature();
		construction.setCurrentBlock(falseBlock);
		Node const10 = construction.newConst(10, modeInt);
		construction.setVariable(varCntrNum, const10);
		Node falseJmp = construction.newJmp();

		// endif
		firm.nodes.Block endifBlock = construction.newBlock();
		endifBlock.addPred(trueJmp);
		endifBlock.addPred(falseJmp);
		endifBlock.mature();
		construction.setCurrentBlock(endifBlock);

		// ------------------------- method return statement --------------------------------

		Node node = construction.newReturn(construction.getCurrentMem(), new Node[] {construction.getVariable(varCntrNum, modeInt)});

		graph.getEndBlock().addPred(node);
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
		// Node addrof_thisY = construction.newMember(projThis, A__y);
		Node storeThisY = construction.newStore(mem, addrof_thisY /* construction.getVariable(varThisNum, modeRef) */, addExpr2);
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

	/**
	 * class A { int i;
	 *
	 * ... main() { A a = new A(); a.i = 10; int[] x = new int[10]; x[0] = 10; } }
	 */
	public static void createCallocGraph()
	{
		// define class
		ClassType classA = new ClassType("A");

		Mode modeInt = Mode.getIs();
		Mode modeRef = Mode.getP();
		Type intType = new PrimitiveType(modeInt);
		Type intArrType = new ArrayType(intType);
		Type reference_to_VoidStar = new PrimitiveType(modeRef);

		// defina A.i
		Entity A__i = new Entity(classA, "A::i", intType);

		// define method "main"
		MethodType mainType = new MethodType(new firm.Type[] {}, new firm.Type[] {});
		Entity mainEntity = new Entity(Program.getGlobalType(), "main(V)V", mainType);

		// define "calloc"
		MethodType calloc_type = new MethodType(new firm.Type[] { intType, intType }, new firm.Type[] { reference_to_VoidStar });
		Entity callocEntity = new Entity(firm.Program.getGlobalType(), "calloc", calloc_type);

		int varA = 0;
		int varXArr = 1;
		int varCount = 2;
		Graph graph = new Graph(mainEntity, varCount);
		Construction construction = new Construction(graph);
		Node c0 = construction.newConst(0, modeInt);
		Node c10 = construction.newConst(10, modeInt);
		Node callocAddr = construction.newAddress(callocEntity);

		//
		// A a = new A();
		//

		// call constructor of A with calloc
		Node num_A = construction.newConst(1, modeInt);
		Node size_A = construction.newSize(modeInt, classA);
		Node callocA = construction.newCall(construction.getCurrentMem(), callocAddr, new Node[] { num_A, size_A }, calloc_type);

		// update memory
		Node memAfterNewA = construction.newProj(callocA, Mode.getM(), Call.pnM);
		construction.setCurrentMem(memAfterNewA);

		// this should cast void* to A* and assign it to a
		// get tuple with method call result
		Node callocAResult = construction.newProj(callocA, Mode.getT(), Call.pnTResult);
		// get first item in a tuple which is reference to new A;
		Node aRef = construction.newProj(callocAResult, modeRef, 0);
		construction.setVariable(varA, aRef);

		//
		// a.i = 10;
		//

		Node addrOfA__i = construction.newMember(construction.getVariable(varA, modeRef), A__i);
		Node storeA_I = construction.newStore(construction.getCurrentMem(), addrOfA__i, c10);
		// update memory
		Node memAfterStore = construction.newProj(storeA_I, Mode.getM(), Store.pnM);
		construction.setCurrentMem(memAfterStore);

		//
		// int[] x = new int[10]
		//

		// allocate memory with calloc
		Node num_X = construction.newConst(10, modeInt);
		Node size_X = construction.newSize(modeInt, intType);
		Node callocX = construction.newCall(construction.getCurrentMem(), callocAddr, new Node[] { num_X, size_X }, calloc_type);

		// update memory
		Node memAfterCallocX = construction.newProj(callocX, Mode.getM(), Call.pnM);
		construction.setCurrentMem(memAfterCallocX);

		// save reference to new memory in x;
		Node callocXResult = construction.newProj(callocX, Mode.getT(), Call.pnTResult);
		Node xArrRef = construction.newProj(callocXResult, modeRef, 0);
		construction.setVariable(varXArr, xArrRef);

		//
		// x[0] = 10;
		//
		Node xArrPtr = construction.getVariable(varXArr, modeRef);
		Node x_index0 = construction.newSel(xArrPtr, c0, intArrType);
		Node storeX_index0 = construction.newStore(construction.getCurrentMem(), x_index0, c10);
		// update memory
		Node memAfterStoreX_index0 = construction.newProj(storeX_index0, Mode.getM(), Store.pnM);
		construction.setCurrentMem(memAfterStoreX_index0);

		//
		// exit main
		//

		Node returnNode = construction.newReturn(construction.getCurrentMem(), new Node[] {});
		returnNode.setPred(0, construction.getCurrentMem());
		graph.getEndBlock().addPred(returnNode);

		construction.setUnreachable();
		construction.finish();
	}
}
