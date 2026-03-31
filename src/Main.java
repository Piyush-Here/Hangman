import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        //Game Setup
        //--------------------

        String filePath = "Words.txt";
        File words = new File(filePath);
        String firstWrongGuess = """
                -------------------------
                            |
                            |
                            |
                            O
               """;
        String secondWrongGuess = """
                -------------------------
                            |
                            |
                            |
                            O
                            .
                            .
                            .
                            .
                            .
               """;
        String thirdWrongGuess = """
                -------------------------
                            |
                            |
                            |
                            O
                            .
                          / .
                         /  .
                            .
                            .
               """;
        String fourthWrongGuess = """
                -------------------------
                            |
                            |
                            |
                            O
                            .
                          / . \\
                         /  .  \\
                            .
                            .
               """;
        String fifthWrongGuess = """
                -------------------------
                            |
                            |
                            |
                            O
                            .
                          / . \\
                         /  .  \\
                            .
                            .
                          /
                         /
                        /
              """;
        String sixthWrongGuess = """
                -------------------------
                            |
                            |
                            |
                            O
                            .
                          / . \\
                         /  .  \\
                            .
                            .
                          /   \\
                         /     \\
                        /       \\
              """;
        String word,gameStatus="Began",guessedWord;
        Random rng = new Random();
        ArrayList<String> wordList=new ArrayList<>();
        ArrayList<Character> guessedLetters = new ArrayList<>();
        int wordLength,NumOfLines=0,choice,chances=6;
        char guessedLetter;

        try(
                BufferedReader reader = new BufferedReader(new FileReader(words));
                Scanner s = new Scanner(System.in)
        )
        {

            while((word= reader.readLine())!=null){
                NumOfLines++;
                wordList.add(word);
            }

            int wordIndex= rng.nextInt(0,NumOfLines);
            word=wordList.get(wordIndex);

            assert word != null;
            wordLength = word.length();

            System.out.println("******************");
            System.out.println("Welcome to Hangman");
            System.out.println("******************");
            System.out.println("\nYou have 6 Chances to guess the word");
            System.out.printf("The word has %d letters\n",wordLength);
            for(int i=0;i<wordLength;i++){
                System.out.print("_");
            }
            System.out.println();

            //Game Loop
            //---------------
            while(!gameStatus.equals("Finished")){
                System.out.print("\n Choose your action :\n1. Guess a letter\n2. Guess the word\n: ");
                choice=s.nextInt();
                s.nextLine();
                switch (choice){
                    case 1 -> {
                        System.out.print("Enter letter :  ");
                        guessedLetter=s.nextLine().charAt(0);
                        if(word.contains(Character.toString(guessedLetter))){
                            
                            for(int i=0;i<wordLength;i++){
                                if(word.charAt(i)==guessedLetter){
                                    guessedLetters.add(guessedLetter);
                                }
                                if(guessedLetters.contains(word.charAt(i))){
                                    System.out.print(word.charAt(i));
                                }else{
                                    System.out.print("_");
                                }
                            }
                        }else{
                            switch(chances--){
                                case 6 -> System.out.println(firstWrongGuess);
                                case 5 -> System.out.println(secondWrongGuess);
                                case 4 -> System.out.println(thirdWrongGuess);
                                case 3 -> System.out.println(fourthWrongGuess);
                                case 2 -> System.out.println(fifthWrongGuess);
                                case 1 -> {
                                    System.out.println(sixthWrongGuess);
                                    System.out.println("YOU LOOSE! the word was "+word);
                                    gameStatus="Finished";
                                }
                            }

                        }
                    }
                    case 2 -> {
                        System.out.println("Enter the word : ");
                        guessedWord=s.nextLine();
                        if(guessedWord.equals(word)){
                            System.out.println("YOU WIN!");
                        }else {
                            switch(chances--){
                                case 6 -> System.out.println(firstWrongGuess);
                                case 5 -> System.out.println(secondWrongGuess);
                                case 4 -> System.out.println(thirdWrongGuess);
                                case 3 -> System.out.println(fourthWrongGuess);
                                case 2 -> System.out.println(fifthWrongGuess);
                                case 1 -> {
                                    System.out.println(sixthWrongGuess);
                                    System.out.println("YOU LOOSE! the word was "+word);
                                    gameStatus="Finished";
                                }
                        }
                    }
                }
            }
        }



    }catch(Exception e){
            System.out.println("Something Went Wrong");
        }
}
}
