package satd.step2;

class Class2 {

    void method1(int code) {
        //this should not be detected
        System.out.print(code);
    }

    double method2() {
        return java.lang.Math.random();
    }

}