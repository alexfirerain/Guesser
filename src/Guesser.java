import java.util.LinkedList;
import java.util.Scanner;

public class Guesser {
    static Scanner con = new Scanner(System.in);
    static int number;                                  // загаданное/отгадываемое число
    static int counter;                                 // счётчик вопросов
    private static boolean bingo;                       // угадано ли число
    final static int N = 1000;                          // верхний предел загадываемого
    static int OUTPUT_WIDTH = N > 20 ?
                              Math.min(20, (int) Math.sqrt(N)) : 20;    // ширина выводимого списка не более 20 в ряд
    static LinkedList<Integer> potentialValues = new LinkedList<>();    // все неисключённые значения отгадываемого
    private static boolean LIMITED_GAME = false;        // переключатель в ограниченный режим игры
    private static int LIMIT = 10;                      // ограничитель ходов для ограниченного режима
    final private static String commands =
                        "Цель игры - угадать число, случайно выбранное ЭВМ из названного диапазона.\n" +
                        "Постарайтесь использовать как можно меньше вопросов.\n" +
                        "Введите вопрос-команду, затем, после приглашения, число.\n" +  // на самом деле можно и вподряд
                        "Допустимые команды:\n" +
                        "\"больше\" - больше ли загаданное число, чем вводимое следом\n" +
                        "\"делится\" - делится ли без остатка загаданное число на вводимое\n" +
                        "\"содержит\" - содержит ли десятичная запись загаданного числа вводимое\n" +
                        "\"равно\" - равно ли загаданное числе вводимому\n" +
                        "\"какие\" - просмотр всех оставшихся возможных значений угадываемого\n" +
                        "\"сдаюсь\" - завершение угадывания, раскрытие загаданного\n" +
                        "\"справка\" - вывод этого текста\n" +
                        "Для выхода из программы после завершения игры ответьте \"нет\"";
    ;

    public static void main(String[] args) {
        // цикл работы программы
        while (true) {
            number = (int) (N * Math.random()) + 1;         // ЭВМ загадывает число
            counter = 0;                                    // иницилизируются служебные переменные
            bingo = false;
            potentialValues.clear();
            for (int i = 1; i <= N; i++) potentialValues.add(i);     // сначала не исключён ни один вариант
            System.out.println("Загадано число от 1 до " + N + " - вводи вопросы: делится / больше / содержит / равно\n" +
                               "(\"справка\" для просмотра всех комманд)");
            if (LIMITED_GAME) System.out.println("Игра ограничена в " + LIMIT + " вопросов.");  // при ограниченной игре
            // цикл угадываний
            requiring:
            while (true) {
                    String command = con.next();
                    switch (command) {
                        case "делится":
                            doesDivide();
                            break;
                        case "больше":
                            isBigger();
                            break;
                        case "содержит":
                            contains();
                            break;
                        case "равно":
                            equals();
                            break;
                        case "какие" :
                            showRemaining();
                            break;
                        case "сдаюсь":
                            surrender();
                            break requiring;
                        case "справка" :
                            System.out.println(commands);
                    }
                    if (bingo) {
                        win();
                        break;
                    }
                    if (LIMITED_GAME && counter == LIMIT) {     // достижение предела вопросов (при ограниченной игре)
                        surrender();
                        break;
                    }
                }
            if (toExit()) break;
        }
    }


    private static void doesDivide() {                       // делится ли нацело
        int divider = getInput("на: ");               // на какое число
        System.out.println(number % divider == 0 ?
                "да" : "нет");
        ++counter;
        if (number % divider == 0) potentialValues.removeIf(n -> n % divider != 0);
        else potentialValues.removeIf(n -> n % divider == 0);
        printCurrentStat();
    }

    private static void isBigger() {                             // больше ли
        int compare = getInput("скольки: ");              // какого числа
        System.out.println(number > compare ?
                "да" : "нет");
        ++counter;
        if (number > compare) potentialValues.removeIf(n -> n <= compare);
        else potentialValues.removeIf(n -> n > compare);
        printCurrentStat();
    }

    private static void contains() {                             // содержит ли
        int cypher = getInput("что: ");                   // какую цифру
        System.out.println(containsDigit(number, cypher) ?
                "да" : "нет");
        ++counter;
        if (containsDigit(number, cypher)) potentialValues.removeIf(n -> !containsDigit(n, cypher));
        else potentialValues.removeIf(n -> containsDigit(n, cypher));
        printCurrentStat();
    }

    private static void equals() {                               // равняется ли
        int guess = getInput("чему: ");                   // какому числу
        if (number == guess) bingo = true;
        System.out.println(bingo ?
                "да" : "нет");
        ++counter;
        if (bingo) potentialValues.removeIf(n -> n != guess);
        else potentialValues.removeIf(n -> n == guess);
        if (!bingo) printCurrentStat();
    }

    private static int getInput(String prompt) {                 // получить ввод
//        String prefixOrInput = "";
//        if (!con.hasNext()) prefixOrInput = con.next().trim(); // попытка поддержки альтернативных спообов ввода
//        if (!prefixOrInput.equals(prompt)) {
            System.out.print(prompt);
            return con.nextInt();
//        }
//        return Integer.parseInt(prefixOrInput);
    }

    private static void showRemaining() {                           // показать остающиеся варианты
        for (int i = 0; i < potentialValues.size(); i++) {
            System.out.printf("%-4d", potentialValues.get(i));
            if ((i + 1) % OUTPUT_WIDTH == 0) System.out.println();
        }
        if (potentialValues.size() % OUTPUT_WIDTH != 0)             // чтоб не пропускать после таблицы лишних строк
            System.out.println();
    }

    private static void surrender() {                               // сдаваться
        System.out.printf("Число было %d, задано %d вопросов.%n", number, counter);
    }

    private static void win() {                                     // побеждать
        System.out.printf("Поздравляю! Число %d угадано за %d вопросов.%n", number, counter );
    }                                                // возможо, за (counter - 1) вопросов, если не считать последний

    private static boolean toExit() {                               // выходиь ли из программы
        System.out.println("\nИграть ещё раз?");
        return "нет".equals(con.next());
    }

    private static boolean containsDigit(int number, int digit) {   // вспомогательная функция
        if (digit > 9 || digit < 0) return false;                   // определяет, содержится ли в числе цифра
        do {
            if (number % 10 == digit) return true;
            number /= 10;
        } while (number != 0);
        return false;
    }
    private static void printCurrentStat() {                        // вывод текущей статистики вопросов и вариантов
        System.out.println("Задано вопросов: " + counter);
        System.out.println("Возможных вариантов ещё " + potentialValues.size());
    }
}
