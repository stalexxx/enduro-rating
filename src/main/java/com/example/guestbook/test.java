package com.example.guestbook;

import java.util.List;

import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class test {
    public static void main(String[] args) {
//        BiFunction<Long, Long, Long> f = ( x , y) ->
//                LongStream.rangeClosed(x, y).reduce((acc, i) -> acc * i).getAsLong();
//
//        Long apply = f.apply(1L, 4L);
//        LongStream.rangeClosed(0, 10).map(test::factorial).forEach(System.out::println);
        System.out.println(sumOfOddNumbersInRange(21, 30));

    }

    public static boolean isPrime(final long number) {
        if (number == 1) {
            return true;
        } else if (number == 2) {
            return true;
        } else {
            return LongStream.rangeClosed(2, Math.round(Math.sqrt(number))).allMatch(x -> number % x != 0);
        }
    }

    public static Stream<String> createBadWordsDetectingStream(String text, List<String> badWords) {
        return Stream.of(text.split(" ")).filter(badWords::contains).distinct().sorted();
    }


    public static IntStream createFilteringStream(IntStream evenStream, IntStream oddStream) {
        return IntStream.concat(evenStream, oddStream).filter(x -> x % 15 == 0).sorted().skip(2);
    }

    public static long factorial(long n) {
        return LongStream.rangeClosed(1, n).reduce(1, (acc, x) -> acc * x);
    }

    public static long sumOfOddNumbersInRange(long start, long end) {
        return LongStream.rangeClosed(start, end).filter(input -> input % 2 != 0).reduce(0, (acc, x) -> acc + x);
    }
    class Department {
        List<Employee> myEmployees;
        private String myCode;

        public List<Employee> getEmployees() {
            return myEmployees;
        }

        public void setEmployees(List<Employee> employees) {
            myEmployees = employees;
        }

        public String getCode() {
            return myCode;
        }
    }

    public static long calcNumberOfEmployees(List<Department> departments, long threshold) {
        return departments.stream().filter(x -> x.getCode().startsWith("111-")).flatMap(x -> x.getEmployees().stream()).filter(x -> x.getSalary() >= threshold).count();
    }

    private class Employee {
        int mySalary;

        public int getSalary() {
            return mySalary;
        }

    }

    private class Salary {
    }
//    public static long calcSumOfCanceledTransOnNonEmptyAccounts(List<Account> accounts) {
//        return accounts.stream().filter(x -> x.getBalance()  > 0).flatMap(x -> x.getTransactions().stream()).filter(it -> it.getState() == State.CANCELED).map(it ->it.getSum()).reduce(0L, (Long acc, Long x) -> acc + x);
//    }
}



