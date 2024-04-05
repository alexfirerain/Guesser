import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.function.BiPredicate;
import java.util.stream.IntStream;

public class Guesser {
    private static final Scanner con = new Scanner(System.in);
    private static int number;                                  // загаданное/отгадываемое число
    private static int counter;                                 // счётчик вопросов
    private static boolean bingo;                       // угадано ли число
    private static int N = 1000;                          // верхний предел загадываемого
    private static int OUTPUT_WIDTH = N > 20 ?
                              Math.min(20, (int) Math.sqrt(N)) : 20;    // ширина выводимого списка не более 20 в ряд
    static LinkedList<Integer> potentialValues = new LinkedList<>();    // все неисключённые значения отгадываемого
    private static boolean LIMITED_GAME = false;        // переключатель в ограниченный режим игры
    private static int LIMIT = 10;                      // ограничитель ходов для ограниченного режима
    final private static String COMMANDS =
            """
                    Цель игры - угадать число, случайно выбранное ЭВМ из названного диапазона.
                    Постарайтесь использовать как можно меньше вопросов.
                    Введите вопрос-команду, затем, после приглашения, число.
                    Допустимые команды:
                    "больше" - больше ли загаданное число, чем вводимое следом
                    "делится" - делится ли без остатка загаданное число на вводимое
                    "содержит" - содержит ли десятичная запись загаданного числа вводимое
                    "равно" - равно ли загаданное числе вводимому
                    "какие" - просмотр всех оставшихся возможных значений угадываемого
                    "сдаюсь" - завершение угадывания, раскрытие загаданного
                    "справка" - вывод этого текста
                    Для выхода из программы после завершения игры ответьте "нет\"""";

    public static void main(String[] args) {
        // цикл работы программы
        while (true) {
            number = (int) (N * Math.random()) + 1;         // ЭВМ загадывает число
            counter = 0;                                    // инициализируются служебные переменные
            bingo = false;
            boolean quit = false;

            potentialValues.clear();
            // сначала не исключён ни один вариант
            IntStream.rangeClosed(1, N).forEach(potentialValues::add);

            System.out.printf("Загадано число от 1 до %d - вводи вопросы: делится / больше / содержит / равно\n" +
                    "(\"справка\" для просмотра всех команд)%n", N);
            if (LIMITED_GAME) System.out.printf("Игра ограничена в %d вопросов.%n", LIMIT);  // при ограниченной игре
            // цикл угадываний
            while (!quit) {
                String command = con.next();
                System.out.println(switch (command) {
                    case "делится" -> performCheck(beDivisible, "на ");
                    case "больше" -> performCheck(beBigger, "скольки: ");
                    case "содержит" -> performCheck(beContaining, "что: ");
                    case "равно" -> performCheck(beEqual, "чему: ");
                    case "повтор" -> performCheck(repeatingDigits, null);
                    case "какие" -> showRemaining();
                    case "сдаюсь" -> {
                        quit = true;
                        yield finish();
                    }
                    case "справка" -> COMMANDS;
                    default -> "Неизвестное значение: " + command;
                });
                if (bingo && !quit || LIMITED_GAME && counter == LIMIT) {     // достижение предела вопросов (при ограниченной игре)
                    System.out.println(finish());
                    quit = true;
                }
            }
            if (toExit()) break;
        }
    }

    private static String performCheck(BiPredicate<Integer, Integer> function, String prompt) {
        ++counter;
        int argument = prompt != null ? getInput(prompt) : 0;
        if (function.test(number, argument)) {
            potentialValues.removeIf(n -> function.negate().test(n, argument));
            if (function == beEqual) bingo = true;
            return "да%nЗадано вопросов: %d%nВозможных вариантов ещё %d%n".formatted(counter, potentialValues.size());
        } else {
            potentialValues.removeIf(n -> function.test(n, argument));
            return "нет%nЗадано вопросов: %d%nВозможных вариантов ещё %d%n".formatted(counter, potentialValues.size());
        }
    }

    private static int getInput(String prompt) {                 // получить ввод
            System.out.print(prompt);
            return con.nextInt();
    }

    private static String showRemaining() {                           // показать остающиеся варианты
        StringBuilder table = new StringBuilder();
        IntStream.range(0, potentialValues.size()).forEach(i -> {
            table.append("%-4d".formatted(potentialValues.get(i)));
            if ((i + 1) % OUTPUT_WIDTH == 0) table.append("\n");
        });
        if (potentialValues.size() % OUTPUT_WIDTH != 0)             // чтоб не пропускать после таблицы лишних строк
            table.append("\n");
        return table.toString();
    }

    private static String finish() {
        return bingo ?
                "Поздравляю! Число %d угадано за %d вопросов.%n".formatted(number, counter) :
                "Число было %d, задано %d вопросов.%n".formatted(number, counter);

    }

    private static boolean toExit() {                               // выходить ли из программы
        System.out.println("\nИграть ещё раз?");
        return "нет".equals(con.next());
    }

    private static boolean containsDigit(int number, int digit) {   // вспомогательная функция
        if (digit > 9 || digit < 0)                                 // определяет, содержится ли в числе цифра
            throw new IllegalArgumentException("Второй аргумент должен быть цифрой от 0 до 9.");

        return splitInDigits(number).contains(digit);
    }

    private static boolean repeatingDigits(int number, int mock) {
        if (Math.abs(number) < 10) return false;

        List<Integer> digits = splitInDigits(number);

        return IntStream.range(0, digits.size() - 1)
                .anyMatch(a ->
                        IntStream.range(a + 1, digits.size())
                            .anyMatch(b -> digits.get(a)
                                    .equals(digits.get(b)))
                );
    }

    private static List<Integer> splitInDigits(Integer number) {
        List<Integer> digits = new ArrayList<>(5);
        number = Math.abs(number);
        do {
            digits.add(number % 10);
            number /= 10;
        } while(number != 0);

        return digits;
    }

    private static final BiPredicate<Integer, Integer> beDivisible = (x, i) -> x % i == 0;
    private static final BiPredicate<Integer, Integer> beBigger = (x, i) -> x > i;
    private static final BiPredicate<Integer, Integer> beContaining = Guesser::containsDigit;
    private static final BiPredicate<Integer, Integer> beEqual = Integer::equals;
    private static final BiPredicate<Integer, Integer> repeatingDigits = Guesser::repeatingDigits;

}
