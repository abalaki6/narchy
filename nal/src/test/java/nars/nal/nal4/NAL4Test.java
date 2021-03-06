//package nars.nal.nal4;
//
//import nars.NAR;
//import nars.Narsese;
//import nars.nal.AbstractNALTest;
//import nars.test.TestNAR;
//import nars.util.signal.RuleTest;
//import org.junit.Ignore;
//import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
//import org.junit.runners.Parameterized;
//
//import java.util.function.Supplier;
//
///**
// *   <neutralization --> (acid,base)>" //en("Neutralization is a relation between an acid and a base. ");
// //     <(\,neutralization,_,base) --> acid> //en("Something that can neutralize a base is an acid.");
// //     <(\,neutralization,acid,_) --> base> //en("Something that can be neutralized by an acid is a base.");
//
// //  <(acid,base) --> reaction> //en("An acid and a base can have a reaction.");
// //     <acid --> (/,reaction,_,base)> //en("Acid can react with base.");
// //     <base --> (/,reaction,acid,_)> //en("A base is something that has a reaction with an acid.");
//
// */
//@Disabled @RunWith(Parameterized.class)
//public class NAL4Test extends AbstractNALTest {
//
//
//    public static final int CYCLES = 450;
//
//    public NAL4Test(Supplier<NAR> b) { super();  }
//
//    @Parameterized.Parameters(name= "{0}")
//    public static Iterable configurations() {
//        return AbstractNALTest.nars(4);
//    }
//
//    @Test
//     public void structural_transformation1()  {
//        TestNAR t = test();
//        t.believe("<(acid,base) --> reaction>",1.0f,0.9f); //en("An acid and a base can have a reaction.");
//        t.mustBelieve(CYCLES, "<acid --> (/,reaction,_,base)>", 1.0f, 0.9f); //en("Acid can react with base.");
//        t.mustBelieve(CYCLES, "<base --> (/,reaction,acid,_)>", 1.0f, 0.9f); //en("A base is something that has a reaction with an acid.");
//    }
//    @Test
//     public void structural_transformation1_DepVar()  {
//        TestNAR t = test();
//        t.believe("reaction(acid,#1)",1.0f,0.9f); //en("An acid and a base can have a reaction.");
//        t.mustBelieve(CYCLES, "<acid --> (/,reaction,_,#1)>", 1.0f, 0.9f); //en("Acid can react with base.");
//        t.mustBelieve(CYCLES, "<#1 --> (/,reaction,acid,_)>", 1.0f, 0.9f); //en("A base is something that has a reaction with an acid.");
//    }
//
//
//    @Test
//      public void structural_transformation2()  {
//        TestNAR tester = test();
//        tester.believe("<acid --> (/,reaction,_,base)>",1.0f,0.9f); //en("Acid can react with base.");
//        tester.mustBelieve(CYCLES, "<(acid,base) --> reaction>", 1.0f, 0.9f); //en("Acid can react with base.");
//
//    }
//
//    @Test
//     public void structural_transformation3()  {
//        TestNAR tester = test();
//        tester.believe("<base --> (/,reaction,acid,_)>",1.0f,0.9f); //en("A base is something that has a reaction with an acid.");
//        tester.mustBelieve(CYCLES*2, "<(acid,base) --> reaction>", 1.0f, 0.9f); //en("Acid can react with base.");
//
//    }
//
//    @Test
//     public void structural_transformation4()  {
//        TestNAR tester = test();
//        tester.believe("<neutralization --> (acid,base)>",1.0f,0.9f); //en("Neutralization is a relation between an acid and a base. ");
//        tester.mustBelieve(CYCLES, "<(\\,neutralization,_,base) --> acid>", 1.0f, 0.9f); //en("Something that can neutralize a base is an acid.");
//        tester.mustBelieve(CYCLES, "<(\\,neutralization,acid,_) --> base>", 1.0f, 0.9f); //en("Something that can be neutralized by an acid is a base.");
//    }
//
//    //PROBABLY NOT CORRECT
////    @Test
////     public void structural_transformation4_extended()  {
////        TestNAR tester = test();
////        tester.believe("<neutralization --> (substance,acid,base)>",1.0f,0.9f);
////        tester.mustBelieve(CYCLES, "<(\\,neutralization,_,acid,base) --> substance>.", 1.0f, 0.9f);
////        tester.mustBelieve(CYCLES, "<(\\,neutralization,substance,_,base) --> acid>.", 1.0f, 0.9f);
////        tester.mustBelieve(CYCLES, "<(\\,neutralization,substance,acid,_) --> base>", 1.0f, 0.9f);
////    }
//
//
//    @Test
//     public void structural_transformation5()  {
//        TestNAR tester = test();
//        tester.believe("<(\\,neutralization,_,base) --> acid>",1.0f,0.9f); //en("Something that can neutralize a base is an acid.");
//        tester.mustBelieve(CYCLES, "<neutralization --> (acid,base)>", 1.0f, 0.9f); //en("Neutralization is a relation between an acid and a base.");
//    }
//
//    //PROBABLY NOT CORRECT
//    @Test
//     public void structural_transformation5_extended()  {
//        TestNAR tester = test();
//        tester.believe("<(\\,neutralization,substance,_,base) --> acid>",1.0f,0.9f);
//        tester.mustBelieve(CYCLES*2, "<neutralization --> (substance,acid,base)>", 1.0f, 0.9f);
//    }
//
//    @Test
//     public void structural_transformation5_extended2c() {
//        TestNAR tester = test();
//        tester.believe("<(\\,neutralization,_,acid,base,reaction) --> substance>", 1.0f, 0.9f);
//        tester.mustBelieve(CYCLES, "<neutralization --> (substance,acid,base,reaction)>", 1.0f, 0.9f);
//    }
//    @Test
//     public void structural_transformation5_extended2a()  {
//        TestNAR tester = test();
//        //tester.log();
//        tester.believe("<(\\,neutralization,substance,_,base,reaction) --> acid>",1.0f,0.9f);
//        tester.mustBelieve(CYCLES, "<neutralization --> (substance,acid,base,reaction)>", 1.0f, 0.9f);
//    }
//
//    @Test
//     public void structural_transformation5_extended2b()  {
//        TestNAR tester = test();
//        tester.believe("<(\\,neutralization,substance,acid,_,reaction) --> base>",1.0f,0.9f);
//        tester.mustBelieve(CYCLES, "<neutralization --> (substance,acid,base,reaction)>", 1.0f, 0.9f);
//    }
//
//    @Test
//     public void structural_transformation5_extended2d()  {
//        TestNAR tester = test();
//        tester.believe("<(\\,neutralization,substance,acid,base,_) --> reaction>",1.0f,0.9f);
//        tester.mustBelieve(CYCLES, "<neutralization --> (substance,acid,base,reaction)>", 1.0f, 0.9f);
//    }
//
//    @Test
//     public void structural_transformation6()  {
//        TestNAR tester = test();
//        tester.believe("<(\\,neutralization,acid,_) --> base>",1.0f,0.9f); //en("Something that can neutralize a base is an acid.");
//        tester.mustBelieve(CYCLES, "<neutralization --> (acid,base)>", 1.0f, 0.9f); //en("Something that can be neutralized by an acid is a base.");
//
//    }
//
//    @Test
//    public void composition_on_both_sides_of_a_statement()  {
//        TestNAR tester = test();
//        tester.believe("<bird --> animal>",1.0f,0.9f); //en("Bird is a type of animal.");
//        tester.askAt(CYCLES/2, "<(bird,plant) --> ?x>"); //en("What is the relation between a bird and a plant?");
//        tester.mustBelieve(CYCLES, "<(bird,plant) --> (animal,plant)>", 1.0f, 0.81f); //en("The relation between bird and plant is a type of relation between animal and plant.");
//    }
//    @Test
//    public void composition_on_both_sides_of_a_statement_question_simultaneous() throws nars.Narsese.NarseseException {
//        TestNAR tester = test();
//        tester.believe("<bird --> animal>",1.0f,0.9f); //en("Bird is a type of animal.");
//        tester.ask("<(bird,plant) --> ?x>"); //en("What is the relation between a bird and a plant?");
//        tester.mustBelieve(CYCLES*2, "<(bird,plant) --> (animal,plant)>", 1.0f, 0.81f); //en("The relation between bird and plant is a type of relation between animal and plant.");
//    }
//    @Test
//    public void composition_on_both_sides_of_a_statement_2() throws Narsese.NarseseException {
//        TestNAR tester = test();
//        //tester.log();
//        tester.believe("<bird --> animal>",1.0f,0.9f); //en("Bird is a type of animal.");
//        tester.ask("<(bird,plant) --> (animal,plant)>");
//        tester.mustBelieve(CYCLES, "<(bird,plant) --> (animal,plant)>", 1.0f, 0.81f); //en("The relation between bird and plant is a type of relation between animal and plant.");
//
//    }
//
////    @Test public void composition_on_both_sides_of_a_statement_long0()  {
////        composition_on_both_sides_of_a_statement_long(0);
////    }
////
////    /** stresses product/image matching rules with a long product */
////    @Test public void composition_on_both_sides_of_a_statement_long1()  {
////        composition_on_both_sides_of_a_statement_long(1);
////    }
////    /** stresses product/image matching rules with a long product */
////    @Test public void composition_on_both_sides_of_a_statement_long2()  {
////        composition_on_both_sides_of_a_statement_long(2);
////    }
////    /** stresses product/image matching rules with a long product */
////    @Test public void composition_on_both_sides_of_a_statement_long3()  {
////        composition_on_both_sides_of_a_statement_long(3);
////    }
////    /** stresses product/image matching rules with a long product */
////    public void composition_on_both_sides_of_a_statement_long(int n)  {
////        String additional = "";
////        for (int i = 0; i < n; i++)
////            additional += ("x" + i) + ',';
////
////        TestNAR tester = test();
////        tester.nar.trace();
////        tester.believe("<neutralization --> reaction>",1.0f,0.9f);
////        tester.ask("<(\\,neutralization," + additional + " acid,_) --> ?x>");
////        tester.mustBelieve(CYCLES*10, "<(\\,neutralization," + additional + " acid,_) --> (\\,reaction," + additional + " acid,_)>", 1.0f, 0.81f);
////
////    }
//
//    @Test
//    public void composition_on_both_sides_of_a_statement2() throws nars.Narsese.NarseseException {
//        TestNAR tester = test();
//        tester.believe("<neutralization --> reaction>",1.0f,0.9f); //en("Neutralization is a type of reaction.");
//        tester.ask("<(\\,neutralization,acid,_) --> ?x>"); //en("What can be neutralized by acid?");
//            //?x could be anything, including #x, or some other non-variable value
//        tester.mustBelieve(CYCLES, "<(\\,neutralization,acid,_) --> (\\,reaction,acid,_)>", 1.0f, 0.81f); //en("What can be neutralized by acid can react with acid.");
//
//    }
//
//    @Test
//    public void composition_on_both_sides_of_a_statement2_2() throws nars.Narsese.NarseseException {
//        TestNAR tester = test();
//        tester.believe("<neutralization --> reaction>",1.0f,0.9f); //en("Neutralization is a type of reaction.");
//        tester.ask("((\\,neutralization,acid,_) --> (\\,reaction,acid,_))");
//        tester.mustBelieve(CYCLES, "<(\\,neutralization,acid,_) --> (\\,reaction,acid,_)>", 1.0f, 0.81f); //en("What can be neutralized by acid can react with acid.");
//
//    }
//
//    @Test
//    public void composition_on_both_sides_of_a_statement3() throws nars.Narsese.NarseseException {
//        test()
//            .believe("<soda --> base>",1.0f,0.9f) //en("Soda is a type of base.");
//            .ask("<(/,neutralization,_,base) --> ?x>") //en("What is something that can neutralize a base?");
//            .mustBelieve(CYCLES, "<(/,neutralization,_,base) --> (/,neutralization,_,soda)>", 1.0f, 0.81f); //en("What can neutraliz base can react with base.");
//    }
//    @Test
//    public void composition_on_both_sides_of_a_statement3b() throws nars.Narsese.NarseseException {
//        test()
//                .believe("<soda --> base>",1.0f,0.9f) //en("Soda is a type of base.");
//                .ask("<(/,neutralization,liquid,_,base) --> ?x>") //en("What is something that can neutralize a base?");
//                .mustBelieve(CYCLES, "<(/,neutralization,liquid,_,base) --> (/,neutralization,liquid,_,soda)>", 1.0f, 0.81f); //en("What can neutraliz base can react with base.");
//    }
//    @Test
//    public void composition_on_both_sides_of_a_statement3c() throws nars.Narsese.NarseseException {
//        test()
//                .believe("<soda --> base>",1.0f,0.9f) //en("Soda is a type of base.");
//                .ask("<(/,neutralization,liquid,base,_) --> ?x>") //en("What is something that can neutralize a base?");
//                .mustBelieve(CYCLES, "<(/,neutralization,liquid,base,_) --> (/,neutralization,liquid,soda,_)>", 1.0f, 0.81f); //en("What can neutraliz base can react with base.");
//    }
//
//
//
//     @Test public void testRecursionForce1() {
//        //    ((X,Z) --> Y), X |- ((X,Z)-->((/,Y,_,Z),Z)), (Belief:StructuralDeduction, Desire:StructuralDeduction)
//        TestNAR t = test();
//
//
//            //.log()
//        t   .believe("(x-->(/,y,_,z))")
//            .askAt(10, "((x,z)-->?a)")
//            .mustBelieve(1750, "((x,z)-->((/,y,_,z),z))", 1f, 0.81f);
//            //.mustBelieve(750, "((x,z)-->(x,(/,y,x,_)))", 1f, 0.81f);
//
//    }
//
//
////    @Test public void missingEdgeCase() {
////        //((<%1 --> %2>, <(|, %1, %3) --> %2>), (<%3 --> %2>,
////        //((<p1 --> p2>, <(|, p1, p3) --> p2>), (<p3 --> p2>,
////        TestNAR tester = test();
////        tester.believe("<p1 --> p2>");
////        tester.believe("<(|, p1, p3) --> p2>");
////        tester.mustBelieve(100, "<p3 --> p2>",
////                1f, 1f, 0.1f, 1f);
////        tester.run(true);
////    }
//
////    @Test public void missingEdgeCase2() {
////        //((<(%1) --> %2>, %2), (<%2 --> (/, %1, _)>, (<Identity --> Truth>, <Identity --> Desire>)))
////        //  ((<(p1) --> p2>, p2), (<p2 --> (/, p1, _)>, (<Identity --> Truth>, <Identity --> Desire>)))
////        RuleTest.get(test(),
////                "<(p1) --> belief:p2>.", "belief:p2.",
////                "<belief:p2 --> (/, _, p1)>.",
////                1.0f, 1.0f, 0.9f, 0.9f);
////    }
//
//
//     @Test public void missingEdgeCase3() {
//        //((<(%1) --> %2>, %1), (<%1 --> (/, %2, _)>, (<Identity --> Truth>, <Identity --> Desire>)))
//        //  ((<(p1) --> p2>, p1), (<p1 --> (/, p2, _)>, (<Identity --> Truth>, <Identity --> Desire>)))
//        RuleTest.get(test(),
//                "<(belief:p1) --> p2>.", "belief:p1.",
//                "<belief:p1 --> (/, p2, _)>.",
//                1.0f, 1.0f, 0.9f, 0.9f);
//    }
//
////    @Test public void missingEdgeCase4() {
////        //((<%1 --> (%2)>, %1), (<(\, %2, _) --> %1>, (<Identity --> Truth>, <Identity --> Desire>)))
////        RuleTest.get(test(),
////                "<belief:p1 --> (p2)>.", "belief:p1.",
////                "<(\\, _, p2) --> belief:p1>.",
////                1.0f, 1.0f, 0.9f, 0.9f);
////    }
//
//     @Test public void missingEdgeCase5() {
//        //((<%1 --> (%2)>, %2), (<(\, %1, _) --> %2>, (<Identity --> Truth>, <Identity --> Desire>)))
//        RuleTest.get(test(),
//                "<p1 --> (belief:p2)>.", "belief:p2.",
//                "<(\\, p1, _) --> belief:p2>.",
//                1.0f, 1.0f, 0.9f, 0.9f);
//    }
//
//
//
//
//}
