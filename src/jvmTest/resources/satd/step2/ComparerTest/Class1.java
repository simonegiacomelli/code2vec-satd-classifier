package satd.step2;

class Class1 {
    void method1(int code) {
        //fixme
        if (cod > 10)
            System.out.println(String.format("code is %d", code));
        for(int i = 0; i<20;i++)
            method2();
    }

    double method2() {
        int offset = 10;
        return java.lang.Math.random() + offset;
    }

}