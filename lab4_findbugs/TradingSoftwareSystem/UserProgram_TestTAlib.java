
class UserProgram_TestTAlib extends UserProgram_Utilities {

    UserProgram_TestTAlib(String symbol) throws Exception {
        super(symbol);

    }

    void run() {

        try {

            Series<Double> x = highSeries().SMA(5);

            for (int i = 0; i < x.size(); i++) {
                System.out.println(x.get(i));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

