package satd.step2;

class Class4 {
    void method1(int code) {
        if (cod > 10)
            System.out.println(String.format("code is %d", code));
    }

    double method2() {
        //TODO this is a hack
        int offset = 10;
        //FIX this will make you barf
        return java.lang.Math.random() + offset;
    }

}