package ru.chepikov;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        File file = new File(args[0]);
        List<Set<String>> groups = findGroups(file);
        writeToFile(groups);
        stopWatch.stop();
        System.out.println("Прошло времени, мс: " + stopWatch.getNanoTime() / 1000000);
    }

    private static class NewWord {
        public String value;
        public int position;

        public NewWord(String value, int position) {
            this.value = value;
            this.position = position;
        }
    }


    private static List<Set<String>> findGroups(File file) {
        List<Map<String, Integer>> wordsToGroupsNumbers = new ArrayList<>();
        List<Set<String>> linesGroups = new ArrayList<>();
        Map<Integer, Integer> mergedGroupNumberToFinalGroupNumber = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] numbers = line.replaceAll("\"", "").split(";", -1);
                if (!Arrays.stream(numbers).allMatch(str -> NumberUtils.isParsable(str) || str.isBlank()))
                    continue;


                TreeSet<Integer> foundInGroups = new TreeSet<>();
                List<NewWord> newWords = new ArrayList<>();
                for (int i = 0; i < numbers.length; i++) {
                    String number = numbers[i];
                    if (wordsToGroupsNumbers.size() == i) {
                        wordsToGroupsNumbers.add(new HashMap<>());
                    }
                    if (number.isEmpty()) {
                        continue;
                    }
                    Integer wordGroupNumber = wordsToGroupsNumbers.get(i).get(number);
                    if (wordGroupNumber != null) {
                        while (mergedGroupNumberToFinalGroupNumber.containsKey(wordGroupNumber))
                            wordGroupNumber = mergedGroupNumberToFinalGroupNumber.get(wordGroupNumber);
                        foundInGroups.add(wordGroupNumber);
                    } else {
                        newWords.add(new NewWord(number, i));
                    }
                }
                int groupNumber;
                if (foundInGroups.isEmpty()) {
                    groupNumber = linesGroups.size();
                    linesGroups.add(new HashSet<>());
                } else {
                    groupNumber = foundInGroups.first();
                }
                for (NewWord newWord : newWords) {
                    wordsToGroupsNumbers.get(newWord.position).put(newWord.value, groupNumber);
                }
                for (int mergeGroupNumber : foundInGroups) {
                    if (mergeGroupNumber != groupNumber) {
                        mergedGroupNumberToFinalGroupNumber.put(mergeGroupNumber, groupNumber);
                        linesGroups.get(groupNumber).addAll(linesGroups.get(mergeGroupNumber));
                        linesGroups.set(mergeGroupNumber, null);
                    }
                }
                linesGroups.get(groupNumber).add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Проблема с записью");
        }
        linesGroups.removeAll(Collections.singleton(null));
        return linesGroups;
    }

    private static void writeToFile(List<Set<String>> groups) {
        File file = new File("output.csv");
        long countOfGroups = 0;

        try (FileWriter writer = new FileWriter(file, false)) {
            for (Set<String> group : groups) {
                if (group.size() > 1) {
                    countOfGroups++;
                    writer.write("Группа " + countOfGroups + "\n");
                    for (String line : group) {
                        writer.write(line + "\n");
                    }
                }
            }
            writer.write("Число групп с более чем одним элементом: " + countOfGroups + "\n");
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
