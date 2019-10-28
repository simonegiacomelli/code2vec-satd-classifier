package satd.step2;

class Class2 {

    void method1(int code) {
        //this should not be detected. This is an orphan comment
    }

    double method2() {
        return java.lang.Math.random();
    }

}