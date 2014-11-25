package compiler.firm;

import firm.*;
import firm.nodes.*;
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
    public static void createEmptyMethod()
    {
        MethodType mainType = new MethodType(new Type[]{}, new Type[]{});
        Entity topEntity = new Entity(Program.getGlobalType(), "empty(V)V", mainType);

        Graph graph = new Graph(topEntity, 0);
        Construction construction = new Construction(graph);

        Node returnNode = construction.newReturn(construction.getCurrentMem(), new Node[]{});
        graph.getEndBlock().addPred(returnNode);

        construction.setUnreachable();
        construction.finish();
    }

    public static void createPrintIntGraph(Entity mainMethodEntity)
    {
        int n_vars = 23;
        Mode modeInt = Mode.getIs();

        Graph graph = new Graph(mainMethodEntity, n_vars);
        Construction construction = new Construction(graph);

        /*
        print_int(42);
         */
        Node c42 = construction.newConst(42, modeInt);
        Node mem = construction.getCurrentMem();

//        construction.ne

//        construction.newCall(mem);

        Node returnNode = construction.newReturn(construction.getCurrentMem(), new Node[]{});
        graph.getEndBlock().addPred(returnNode);

        construction.setUnreachable();
        construction.finish();
    }

}
