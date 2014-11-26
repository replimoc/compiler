package compiler.semantic;

import org.apache.commons.math3.analysis.function.Constant;
import org.junit.Test;

import firm.ClassType;
import firm.CompoundType;
import firm.Construction;
import firm.Dump;
import firm.Entity;
import firm.Firm;
import firm.Graph;
import firm.MethodType;
import firm.Mode;
import firm.PrimitiveType;
import firm.Program;
import firm.Type;
import firm.nodes.Node;

public class FirmMethodsTest {

	@Test
	public void testjFirmInit() {
		Firm.init();
		System.out.printf("Initialized Firm Version: %1s.%2s\n",
				Firm.getMajorVersion(), Firm.getMinorVersion());

		PrimitiveType intType = new PrimitiveType(Mode.getIs());
		PrimitiveType floatType = new PrimitiveType(Mode.getF());
		
		ClassType globalType = Program.getGlobalType();
		
		// create method named foo(int, int) : int
		MethodType methodType = new MethodType(new Type[] { intType, intType },
				new Type[] { intType });
		Entity methodEntity = new Entity(globalType, "foo", methodType);
		methodEntity.setLdIdent("foo");
		
		int n_vars = 2 + 1; // two parameters + local var
		Graph graph = new Graph(methodEntity, n_vars);
		Construction construction = new Construction(graph);
		
		// create a const node
		Node returnConst = construction.newConst(42, Mode.getIs());
		// set var 2 to const node
		construction.setVariable(2, returnConst);
		
		Node nreturn = construction.newReturn(construction.getCurrentMem(), new Node[] { returnConst });
		graph.getEndBlock().addPred(nreturn);
		
		construction.setUnreachable();
		
		construction.finish();
		Dump.dumpGraph(graph, "-after-construction");
	}
}
