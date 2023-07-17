package com.english.irregular_verbs.service;

import com.english.irregular_verbs.config.BotConfig;
import com.english.irregular_verbs.model.*;
import com.english.irregular_verbs.repositories.*;
import com.vdurmont.emoji.EmojiParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.*;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private static final Logger loggerError = LoggerFactory.getLogger("logger.error");
    private static final Logger loggerInfo = LoggerFactory.getLogger("logger.info");
    static final String ERROR_TEXT = "Error occurred: ";
    private final IrregularVerbRepository irregularVerbRepository;
    private final UserRepository userRepository;
    private final VerbGroup1Repository verbGroup1Repository;
    private final VerbGroup2Repository verbGroup2Repository;
    private final VerbGroup3Repository verbGroup3Repository;
    private final VerbGroup4Repository verbGroup4Repository;
    private final VerbGroup5Repository verbGroup5Repository;
    private final VerbGroup6Repository verbGroup6Repository;
    private final BotConfig config;
    List<BotCommand> listOfCommands = new ArrayList<>();
    Optional<VerbGroup1> randomVerbGroup1;
    Optional<VerbGroup2> randomVerbGroup2;
    Optional<VerbGroup3> randomVerbGroup3;
    Optional<VerbGroup4> randomVerbGroup4;
    Optional<VerbGroup5> randomVerbGroup5;
    Optional<VerbGroup6> randomVerbGroup6;
    final String instructions = "\uD83D\uDCDAДля того, щоб вивчати дієслова:" + "\n" +
            "✅ Натисніть \"Вивчати дієслова\";" + "\n" +
            "✅ Обирайте будь-яку группу для вивчення, та вивчайте дієслова;" + "\n" +
            "✅ Натисніть \"Перевірити знання\", якщо хочете перевірити свої знаяння дієслів з обраної групи;" + "\n" +
            "✅ Бот рандомно надішле вам дієслово украінською мовою з обраної групи;" + "\n" +
            "✅ Відправляйте у відповідь 3 форми дієслова англійською, а бот відповість чи правильна ваша відповідь;" + "\n" +
            "✅ Якщо ваша відповідь неправильна продовжуйте вводити 3 форми поки не відповісте правильно;" + "\n" +
            "✅ Якщо ваша відповідь правильна, тисність знову \"Перевірити знання\" та отрамайте нове рандомне дієслово;" + "\n" +
            "✅ Продовжуйте вивчення дієслів та перевірку звоїх знать у зручному для вас темпі та поки не досягнете повного вивчення дієслів;" + "\n" +
            "✅ Щоб обрати іншу групу дєслів натисніть \"Вивчати дієслова\"" + "\n\n" +
            "🔍 Для того, щоб перевірити чи правильне дієслово:" + "\n" +
            "✅ Ви можете просто ввести будь-яку форму дієслова, яке хочете перевірити або спочатку натиснути \"Перевірити дієслово\";" + "\n" +
            "✅ Якщо дієслово неправильне - бот у відповідь надішле вам усі три форми з перекладом на українську мову;" + "\n" +
            "✅ У іншому випадку бот відповість - що це дієслово правильне.";
    final String verbGroups = " - Дієслова в яких всі три форми однакові /group1 " + "\n" + " " +
            "- Дієслова в яких перша та третя форми однакові /group2 " + "\n" + " " +
            "- Дієслова в яких друга та третя форми однакові /group3 " + "\n" + " " +
            "- Дієслова в яких третя форма закінчується на \"EN\" /group4 " + "\n" + " " +
            "- Дієслова в яких третя форма закінчується на \"OWN\" \"AWN \" /group5 " + "\n" + " " +
            "- Дієслова в яких всі три форми різні /group6 ";

    @Autowired
    public TelegramBot(IrregularVerbRepository irregularVerbRepository,
                       UserRepository userRepository, VerbGroup1Repository verbGroup1Repository, VerbGroup2Repository verbGroup2Repository,
                       VerbGroup3Repository verbGroup3Repository, VerbGroup4Repository verbGroup4Repository,
                       VerbGroup5Repository verbGroup5Repository, VerbGroup6Repository verbGroup6Repository, BotConfig config) {
        this.irregularVerbRepository = irregularVerbRepository;
        this.userRepository = userRepository;
        this.verbGroup1Repository = verbGroup1Repository;
        this.verbGroup2Repository = verbGroup2Repository;
        this.verbGroup3Repository = verbGroup3Repository;
        this.verbGroup4Repository = verbGroup4Repository;
        this.verbGroup5Repository = verbGroup5Repository;
        this.verbGroup6Repository = verbGroup6Repository;
        this.config = config;

        listOfCommands.add(new BotCommand("/start", "Почати роботу"));
        listOfCommands.add(new BotCommand("/help", "Інструкція з використання"));
        listOfCommands.add(new BotCommand("/suggestions", "Скарги та пропозиції"));
        listOfCommands.add(new BotCommand("/donate", "На каву розробнику"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            loggerError.error("Error setting bot's command list: " + e.getMessage());
        }
    }


    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()

        ) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            String incomingText = message.getText();
            String[] messageArray = message.getText().split("[\\s,]+");
            System.out.println("getUserName сначала--> " + update.getMessage().getFrom().getUserName());
            System.out.println("getId сначала--> " + update.getMessage().getFrom().getId());
            System.out.println("getChatId сначала--> " + update.getMessage().getChatId());

            if (incomingText.equals(listOfCommands.get(0).getCommand())) {
                registerUser(message);
                loggerInfo.info("User " + update.getMessage().getChat().getFirstName() + " push the " + listOfCommands.get(0));
                sendMessage(chatId, "Hi " + update.getMessage().getChat().getFirstName() + ", nice to meet you ☺");
                mainKeyboard(chatId);
//                groupsAndCheckVerbsButton(chatId);

            } else if (incomingText.equals(listOfCommands.get(1).getCommand())) {
                sendMessage(chatId, instructions);
                loggerInfo.info("User " + update.getMessage().getChat().getFirstName() + " push the " + listOfCommands.get(1));
            } else if (incomingText.equals(listOfCommands.get(2).getCommand())) {
                suggestionLink(chatId);
                loggerInfo.info("User " + update.getMessage().getChat().getFirstName() + " push the " + listOfCommands.get(2));
            } else if (incomingText.equals(listOfCommands.get(3).getCommand())) {
                donateLink(chatId);
                loggerInfo.info("User " + update.getMessage().getChat().getFirstName() + " push the " + listOfCommands.get(3));

            } else if (incomingText.contains("/send") && config.getOwnerId() == chatId) {
                var textToSend = EmojiParser.parseToUnicode(incomingText.substring(incomingText.indexOf(" ")));
                var users = userRepository.findAll();
                for (User user : users) {
                    sendMessage(user.getChatId(), textToSend);
                    loggerInfo.info("Message: " + textToSend + " sent from the bot's owner");
                }

            } else if (incomingText.contains("Вивчати дієслова")) {
                sendMessage(chatId, "Вибирай будь-яку групу дієслів, вивчай дієслова та перевіряй свої знання");
                sendMessage(chatId, verbGroups);
                deleteMessage(chatId, message.getMessageId());


            } else if (incomingText.equals("/group1")) {
                Optional<User> optionalUser = userRepository.findById(chatId);
                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();
                    user.setGroupIndicator(1);
                    userRepository.save(user);
                    System.out.println("getUserName из incomingText.equals(\"/group1\"-->> " + userRepository.findById(chatId).get().getUserName());
                }
                var verbs = verbGroup1Repository.findAll();
                sendVerbsGroup(verbs, chatId);
                checkKnowledgeAndVerbButton(chatId);

            } else if (incomingText.equals("/group2")) {

                Optional<User> optionalUser = userRepository.findById(chatId);

                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();
                    user.setGroupIndicator(2);
                    userRepository.save(user);
                    System.out.println("getUserName из incomingText.equals(\"/group2\"-->> " + userRepository.findById(chatId).get().getUserName());
                }

                var verbs = verbGroup2Repository.findAll();
                sendVerbsGroup(verbs, chatId);
                checkKnowledgeAndVerbButton(chatId);

            } else if (incomingText.equals("/group3")) {
                Optional<User> optionalUser = userRepository.findById(chatId);

                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();
                    user.setGroupIndicator(3);
                    userRepository.save(user);
                    System.out.println("getUserName из incomingText.equals(\"/group3\"-->> " + userRepository.findById(chatId).get().getUserName());
                }

                var verbs = verbGroup3Repository.findAll();
                sendVerbsGroup(verbs, chatId);
                checkKnowledgeAndVerbButton(chatId);
            } else if (incomingText.equals("/group4")) {
                Optional<User> optionalUser = userRepository.findById(chatId);
                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();
                    user.setGroupIndicator(4);
                    userRepository.save(user);
                    System.out.println("getUserName из incomingText.equals(\"/group4\"-->> " + userRepository.findById(chatId).get().getUserName());
                }
                var verbs = verbGroup4Repository.findAll();
                sendVerbsGroup(verbs, chatId);
                checkKnowledgeAndVerbButton(chatId);
            } else if (incomingText.equals("/group5")) {
                Optional<User> optionalUser = userRepository.findById(chatId);
                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();
                    user.setGroupIndicator(5);
                    userRepository.save(user);
                    System.out.println("getUserName из incomingText.equals(\"/group5\"-->> " + userRepository.findById(chatId).get().getUserName());
                }
                var verbs = verbGroup5Repository.findAll();
                sendVerbsGroup(verbs, chatId);
                checkKnowledgeAndVerbButton(chatId);
            } else if (incomingText.equals("/group6")) {
                Optional<User> optionalUser = userRepository.findById(chatId);
                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();
                    user.setGroupIndicator(6);
                    userRepository.save(user);
                    System.out.println("getUserName из incomingText.equals(\"/group6\"-->> " + userRepository.findById(chatId).get().getUserName());
                }
                var verbs = verbGroup6Repository.findAll();
                sendVerbsGroup(verbs, chatId);
                checkKnowledgeAndVerbButton(chatId);
            } else if (incomingText.contains("Перевірити дієслово")) {
                System.out.println("Перевірити дієслово длина -->>" + messageArray.length);
                deleteMessage(chatId, message.getMessageId());
                sendMessage(chatId, "Введіть будь-яку форму дієслова одним словом");

            } else if (messageArray.length == 1 && isEnglishText(incomingText)) {
                System.out.println("Зашёл в messageArray.length == 1 и searchVerb длина: -->" + messageArray.length);
                var verbs = irregularVerbRepository.findAll();

                searchVerb(verbs, incomingText, chatId);

                checkKnowledgeAndVerbButton(chatId);

                loggerInfo.info("User " + update.getMessage().getChat().getFirstName() + " found word: " + incomingText);

            } else {

                Optional<User> optionalUser = userRepository.findById(chatId);
                if (optionalUser.isPresent()) {
                    int indicator = optionalUser.get().getGroupIndicator();

                    switch (indicator) {
                        case 1:
                            compareWithGroup1(chatId, message, randomVerbGroup1);
                            System.out.println("verbGroup1 из messageArray.length else и compareWithRandom-->> " + randomVerbGroup1);
                            break;
                        case 2:
                            compareWithGroup2(chatId, message, randomVerbGroup2);
                            System.out.println("verbGroup2 из messageArray.length else и compareWithRandom-->> " + randomVerbGroup2);
                            break;
                        case 3:
                            if (randomVerbGroup3.isPresent()) {
                                compareWithGroup3(chatId, message, randomVerbGroup3);
                            } else throw new NoSuchElementException("EXEPTION");
                            break;
                        case 4:
                            compareWithGroup4(chatId, message, randomVerbGroup4);
                            System.out.println("verbGroup4 из messageArray.length else и compareWithRandom-->> " + randomVerbGroup4);
                            break;
                        case 5:
                            compareWithGroup5(chatId, message, randomVerbGroup5);
                            System.out.println("verbGroup5 из messageArray.length else и compareWithRandom-->> " + randomVerbGroup5);
                            break;
                        case 6:
                            compareWithGroup6(chatId, message, randomVerbGroup6);
                            System.out.println("verbGroup5 из messageArray.length else и compareWithRandom-->> " + randomVerbGroup6);
                            break;
                    }
                }

            }

        } else if (update.hasCallbackQuery()) {
            String callBackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            System.out.println("messageId-->> " + messageId);
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            // TODO: 19.05.2023 здесь будем получать юзера и в зависимости от его индикатора включать ниже random определнной группы

            if (callBackData.equals("CheckKnowledge")) {

                Optional<User> optionalUser = userRepository.findById(chatId);
                if (optionalUser.isPresent()) {
                    int groupIndicator = optionalUser.get().getGroupIndicator();
                    randomVerbFromGroup(groupIndicator, chatId);


                }


            } else if (callBackData.equals("CheckVerb")) {
                sendMessage(chatId, "Введіть одним словом будь-яку форму дієслова");

            }
        }

    }

    public void randomVerbFromGroup(int indicator, long chatId) {
        String randomTranslate; // TODO: 16.07.2023 Нужно ли = null
        long range = 0;
        int number = 0;

        switch (indicator) {
            case 1:
                range = verbGroup1Repository.count();
                System.out.println("Количество глаголов в гуппе2 -->> " + range);
                number = (int) (Math.random() * range) + 1;
                System.out.println("Рандомное число из randomVerbFromGroup -->> " + number + " и indicator = " + indicator);
                randomVerbGroup1 = verbGroup1Repository.findById(number);
                if (randomVerbGroup1.isPresent()) {
                    randomTranslate = randomVerbGroup1.get().getTranslate();
                    sendMessage(chatId, randomTranslate);
                } else
                    sendMessage(chatId, "Натисніть кнопку \"Перевірити знання\""); // TODO: 16.07.2023 нужна ли эта строка - не работает
                break;
            case 2:
                range = verbGroup2Repository.count();
                System.out.println("Количество глаголов в гуппе2 -->> " + range);
                number = (int) (Math.random() * range) + 1;
                System.out.println("Рандомное число из randomVerbFromGroup -->> " + number + " и indicator = " + indicator);
                randomVerbGroup2 = verbGroup2Repository.findById(number);
                if (randomVerbGroup2.isPresent()) {
                    randomTranslate = randomVerbGroup2.get().getTranslate();
                    sendMessage(chatId, randomTranslate);
                }
                break;
            case 3:
                range = verbGroup3Repository.count();
                System.out.println("Количество глаголов в гуппе3 -->> " + range);
                number = (int) (Math.random() * range) + 1;
                System.out.println("Рандомное число из randomVerbFromGroup -->> " + number + " и indicator = " + indicator);
                randomVerbGroup3 = verbGroup3Repository.findById(number);
                if (randomVerbGroup3.isPresent()) {
                    randomTranslate = randomVerbGroup3.get().getTranslate();
                    sendMessage(chatId, randomTranslate);
                }
                break;
            case 4:
                range = verbGroup4Repository.count();
                System.out.println("Количество глаголов в гуппе4 -->> " + range);
                number = (int) (Math.random() * range) + 1;
                System.out.println("Рандомное число из randomVerbFromGroup -->> " + number + " и indicator = " + indicator);
                randomVerbGroup4 = verbGroup4Repository.findById(number);
                if (randomVerbGroup4.isPresent()) {
                    randomTranslate = randomVerbGroup4.get().getTranslate();
                    sendMessage(chatId, randomTranslate);
                }
                break;
            case 5:
                range = verbGroup5Repository.count();
                System.out.println("Количество глаголов в гуппе5 -->> " + range);
                number = (int) (Math.random() * range) + 1;
                System.out.println("Рандомное число из randomVerbFromGroup -->> " + number + " и indicator = " + indicator);
                randomVerbGroup5 = verbGroup5Repository.findById(number);
                if (randomVerbGroup5.isPresent()) {
                    randomTranslate = randomVerbGroup5.get().getTranslate();
                    sendMessage(chatId, randomTranslate);
                }
                break;
            case 6:
                range = verbGroup6Repository.count();
                System.out.println("Количество глаголов в гуппе6 -->> " + range);
                number = (int) (Math.random() * range) + 1;
                System.out.println("Рандомное число из randomVerbFromGroup -->> " + number + " и indicator = " + indicator);
                randomVerbGroup6 = verbGroup6Repository.findById(number);
                if (randomVerbGroup6.isPresent()) {
                    randomTranslate = randomVerbGroup6.get().getTranslate();
                    sendMessage(chatId, randomTranslate);
                }
        }

    }

    public void compareWithGroup1(long chatId, Message message, Optional<VerbGroup1> randomVerbGroup1) {

        String[] userAnswer = message.getText().toLowerCase().split("[\\s,]+");

        if (randomVerbGroup1.isPresent()) {
            String[] infinitiveArray = randomVerbGroup1.get().getInfinitive().split(" ");
            String[] pastIndefiniteArray = randomVerbGroup1.get().getPastIndefinite().split("[\\s,]+");
            String[] pastParticipleArray = randomVerbGroup1.get().getPastParticiple().split("[\\s,]+");

            if (userAnswer.length == 3
                    && userAnswer[0].equals(infinitiveArray[0])
                    && userAnswer[1].equals(pastIndefiniteArray[0])
                    && userAnswer[2].equals(pastParticipleArray[0])
            ) {
                sendMessage(chatId, "Це правильна відповідь \uD83D\uDC4F");
                checkKnowledgeAndVerbButton(chatId);
            } else if (userAnswer.length == 3
                    && userAnswer[0].equals(infinitiveArray[0])
                    && userAnswer[1].equals(pastIndefiniteArray[0])
                    && userAnswer[2].equals(pastParticipleArray[pastParticipleArray.length - 2])
            ) {
                sendMessage(chatId, "Це правильна відповідь \uD83D\uDC4F");
                checkKnowledgeAndVerbButton(chatId);
            } else if (userAnswer.length == 3
                    && userAnswer[0].equals(infinitiveArray[0])
                    && userAnswer[1].equals(pastIndefiniteArray[pastIndefiniteArray.length - 2])
                    && userAnswer[2].equals(pastParticipleArray[0])
            ) {
                sendMessage(chatId, "Це правильна відповідь \uD83D\uDC4F");
                checkKnowledgeAndVerbButton(chatId);
            } else if (userAnswer.length == 3
                    && userAnswer[0].equals(infinitiveArray[0])
                    && userAnswer[1].equals(pastIndefiniteArray[pastIndefiniteArray.length - 2])
                    && userAnswer[2].equals(pastParticipleArray[pastParticipleArray.length - 2])
            ) {
                sendMessage(chatId, "Це правильна відповідь \uD83D\uDC4F");
                checkKnowledgeAndVerbButton(chatId);
            } else sendMessage(chatId, "❗Відповідь неправильна, спробуйте ще раз");
        }

    }

    public void compareWithGroup2(long chatId, Message message, Optional<VerbGroup2> randomVerbGroup2) {

        String[] userAnswer = message.getText().toLowerCase().split("[\\s,]+");

        if (randomVerbGroup2.isPresent()) {

            String[] infinitiveArray = randomVerbGroup2.get().getInfinitive().split(" ");
            String[] pastIndefiniteArray = randomVerbGroup2.get().getPastIndefinite().split("[\\s,]+");
            String[] pastParticipleArray = randomVerbGroup2.get().getPastParticiple().split("[\\s,]+");

            if (userAnswer.length == 3 && !userAnswer[0].equals(userAnswer[1])
                    && userAnswer[0].equals(userAnswer[2])
                    && userAnswer[0].equals(infinitiveArray[0])
                    && userAnswer[1].equals(pastIndefiniteArray[0])
                    && userAnswer[2].equals(pastParticipleArray[0])) {
                sendMessage(chatId, "Це правильна відповідь \uD83D\uDC4F");
                checkKnowledgeAndVerbButton(chatId);

            } else sendMessage(chatId, "❗Відповідь неправильна, спробуйте ще раз");

        }
    }

    public void compareWithGroup3(long chatId, Message message, Optional<VerbGroup3> randomVerbGroup3) {

        String[] userAnswer = message.getText().toLowerCase().split("[\\s,]+");

        if (randomVerbGroup3.isPresent()) {
            System.out.println("рандомный глагол из метода compareWithGroup3 -->> " + randomVerbGroup3);
            String[] infinitiveArray = randomVerbGroup3.get().getInfinitive().split(" ");
            String[] pastIndefiniteArray = randomVerbGroup3.get().getPastIndefinite().split("[\\s,]+");
            String[] pastParticipleArray = randomVerbGroup3.get().getPastParticiple().split("[\\s,]+");

            if (userAnswer.length == 3
                    && userAnswer[0].equals(infinitiveArray[0])
                    && userAnswer[1].equals(pastIndefiniteArray[0])
                    && userAnswer[2].equals(pastParticipleArray[0])
            ) {
                sendMessage(chatId, "Це правильна відповідь \uD83D\uDC4F");
                checkKnowledgeAndVerbButton(chatId);
            } else if (userAnswer.length == 3
                    && userAnswer[0].equals(infinitiveArray[0])
                    && userAnswer[1].equals(pastIndefiniteArray[0])
                    && userAnswer[2].equals(pastParticipleArray[pastParticipleArray.length - 2])
            ) {
                sendMessage(chatId, "Це правильна відповідь \uD83D\uDC4F");
                checkKnowledgeAndVerbButton(chatId);
            } else if (userAnswer.length == 3
                    && userAnswer[0].equals(infinitiveArray[0])
                    && userAnswer[1].equals(pastIndefiniteArray[pastIndefiniteArray.length - 2])
                    && userAnswer[2].equals(pastParticipleArray[0])) {
                sendMessage(chatId, "Це правильна відповідь \uD83D\uDC4F");
                checkKnowledgeAndVerbButton(chatId);
            } else if (userAnswer.length == 3
                    && userAnswer[0].equals(infinitiveArray[0])
                    && userAnswer[1].equals(pastIndefiniteArray[pastIndefiniteArray.length - 2])
                    && userAnswer[2].equals(pastParticipleArray[pastParticipleArray.length - 2])
            ) {
                sendMessage(chatId, "Це правильна відповідь \uD83D\uDC4F");
                checkKnowledgeAndVerbButton(chatId);
            } else
                sendMessage(chatId, "❗Відповідь неправильна, спробуйте ще раз");
        }

    }

    public void compareWithGroup4(long chatId, Message message, Optional<VerbGroup4> randomVerbGroup4) {

        String[] userAnswer = message.getText().toLowerCase().split("[\\s,]+");

        if (randomVerbGroup4.isPresent()) {
            String[] infinitiveArray = randomVerbGroup4.get().getInfinitive().split(" ");
            String[] pastIndefiniteArray = randomVerbGroup4.get().getPastIndefinite().split("[\\s,]+");
            String[] pastParticipleArray = randomVerbGroup4.get().getPastParticiple().split("[\\s,]+");

            if (userAnswer.length == 3
                    && userAnswer[0].equals(infinitiveArray[0])
                    && userAnswer[1].equals(pastIndefiniteArray[0])
                    && userAnswer[2].equals(pastParticipleArray[0])
            ) {
                sendMessage(chatId, "Це правильна відповідь \uD83D\uDC4F");
                checkKnowledgeAndVerbButton(chatId);
            } else if (userAnswer.length == 3
                    && userAnswer[0].equals(infinitiveArray[0])
                    && userAnswer[1].equals(pastIndefiniteArray[0])
                    && userAnswer[2].equals(pastParticipleArray[pastParticipleArray.length - 2])
            ) {
                sendMessage(chatId, "Це правильна відповідь \uD83D\uDC4F");
                checkKnowledgeAndVerbButton(chatId);
            } else if (userAnswer.length == 3
                    && userAnswer[0].equals(infinitiveArray[0])
                    && userAnswer[1].equals(pastIndefiniteArray[pastIndefiniteArray.length - 2])
                    && userAnswer[2].equals(pastParticipleArray[0])) {
                sendMessage(chatId, "Це правильна відповідь \uD83D\uDC4F");
                checkKnowledgeAndVerbButton(chatId);
            } else if (userAnswer.length == 3
                    && userAnswer[0].equals(infinitiveArray[0])
                    && userAnswer[1].equals(pastIndefiniteArray[pastIndefiniteArray.length - 2])
                    && userAnswer[2].equals(pastParticipleArray[pastParticipleArray.length - 2])
            ) {
                sendMessage(chatId, "Це правильна відповідь \uD83D\uDC4F");
                checkKnowledgeAndVerbButton(chatId);
            } else
                sendMessage(chatId, "❗Відповідь неправильна, спробуйте ще раз");
        }

    }

    public void compareWithGroup5(long chatId, Message message, Optional<VerbGroup5> randomVerbGroup5) {

        String[] userAnswer = message.getText().toLowerCase().split("[\\s,]+");

        if (randomVerbGroup5.isPresent()) {
            String[] infinitiveArray = randomVerbGroup5.get().getInfinitive().split(" ");
            String[] pastIndefiniteArray = randomVerbGroup5.get().getPastIndefinite().split("[\\s,]+");
            String[] pastParticipleArray = randomVerbGroup5.get().getPastParticiple().split("[\\s,]+");

            if (userAnswer.length == 3
                    && userAnswer[0].equals(infinitiveArray[0])
                    && userAnswer[1].equals(pastIndefiniteArray[0])
                    && userAnswer[2].equals(pastParticipleArray[0])
            ) {
                sendMessage(chatId, "Це правильна відповідь \uD83D\uDC4F");
                checkKnowledgeAndVerbButton(chatId);
            } else if (userAnswer.length == 3
                    && userAnswer[0].equals(infinitiveArray[0])
                    && userAnswer[1].equals(pastIndefiniteArray[0])
                    && userAnswer[2].equals(pastParticipleArray[pastParticipleArray.length - 2])
            ) {
                sendMessage(chatId, "Це правильна відповідь \uD83D\uDC4F");
                checkKnowledgeAndVerbButton(chatId);
            } else
                sendMessage(chatId, "❗Відповідь неправильна, спробуйте ще раз");
        }

    }

    public void compareWithGroup6(long chatId, Message message, Optional<VerbGroup6> randomVerbGroup6) {

        String[] userAnswer = message.getText().toLowerCase().split("[\\s,]+");

        if (randomVerbGroup6.isPresent()) {
            String[] infinitiveArray = randomVerbGroup6.get().getInfinitive().split(" ");
            String[] pastIndefiniteArray = randomVerbGroup6.get().getPastIndefinite().split("[\\s,]+");
            String[] pastParticipleArray = randomVerbGroup6.get().getPastParticiple().split("[\\s,]+");

            if (userAnswer.length == 3
                    && userAnswer[0].equals(infinitiveArray[0])
                    && userAnswer[1].equals(pastIndefiniteArray[0])
                    && userAnswer[2].equals(pastParticipleArray[0])

            ) {
                sendMessage(chatId, "Це правильна відповідь \uD83D\uDC4F");
                checkKnowledgeAndVerbButton(chatId);
            } else if (userAnswer.length == 3
                    && userAnswer[0].equals(infinitiveArray[0])
                    && userAnswer[1].equals(pastIndefiniteArray[pastIndefiniteArray.length - 2])
                    && userAnswer[2].equals(pastParticipleArray[0])

            ) {
                sendMessage(chatId, "Це правильна відповідь \uD83D\uDC4F");
                checkKnowledgeAndVerbButton(chatId);
            } else if (userAnswer.length == 3
                    && userAnswer[0].equals(infinitiveArray[0])
                    && userAnswer[1].equals(pastIndefiniteArray[0])
                    && userAnswer[2].equals(pastParticipleArray[pastParticipleArray.length - 2])
            ) {
                sendMessage(chatId, "Це правильна відповідь \uD83D\uDC4F");
                checkKnowledgeAndVerbButton(chatId);
            } else
                sendMessage(chatId, "❗Відповідь неправильна, спробуйте ще раз");
        }

    }

    public void searchVerb(List<IrregularVerb> verbs, String messageText, long chatId) {
        boolean flag = false;
        for (IrregularVerb verb : verbs) {

            String[] infinitiveArray = verb.getInfinitive().split(" ");
            String[] pastIndefiniteArray = verb.getPastIndefinite().split("[\\s,]+");
            String[] pastParticipleArray = verb.getPastParticiple().split("[\\s,]+");

            if (messageText.toLowerCase().equals(infinitiveArray[0]) || messageText.toLowerCase().equals(pastIndefiniteArray[0])
                    || messageText.toLowerCase().equals(pastIndefiniteArray[pastIndefiniteArray.length - 2])
                    || messageText.toLowerCase().equals(pastParticipleArray[0])
                    || messageText.toLowerCase().equals(pastParticipleArray[pastParticipleArray.length - 2])) {
                flag = true;
                sendMessage(chatId, "\uD83D\uDCCC " + verb.getInfinitive() + "\n" + "\uD83D\uDCCC " + verb.getPastIndefinite() + "\n" + "\uD83D\uDCCC " + verb.getPastParticiple() + "\n" + "✅ " + verb.getTranslate());
            }

        }
        if (!flag && !messageText.equals("Перевірити дієслово")) {
            sendMessage(chatId, "❗Це правильне дієслово");
        }

    }

    public void checkKnowledgeAndVerbButton(long chatId) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Що ви хочете робити далі?\uD83D\uDE42");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        var checkKnowledge = new InlineKeyboardButton();
        checkKnowledge.setText("\uD83E\uDDD1\u200D\uD83C\uDF93Перевірити знання");
        checkKnowledge.setCallbackData("CheckKnowledge");
        var checkVerb = new InlineKeyboardButton();
        checkVerb.setText("\uD83D\uDD0DПеревіріти дієслово");
        checkVerb.setCallbackData("CheckVerb");

        rowInline.add(checkKnowledge);
        rowInline.add(checkVerb);
        rowsInline.add(rowInline);
        markupInLine.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInLine);

        executeMessage(message);

    }

    public void mainKeyboard(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));

        sendMessage.setText("ℹ️ Що ви хочете зробити?\uD83D\uDE42");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("\uD83D\uDCDAВивчати дієслова");
        row.add("\uD83D\uDD0DПеревірити дієслово");
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);

        sendMessage.setReplyMarkup(keyboardMarkup);

        executeMessage(sendMessage);

    }

    public void suggestionLink(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("<b>Введіть ваші скарги або пропозиції клікнувши на кнопку нижче</b>");
        message.setParseMode(ParseMode.HTML);
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        var reviewButton = new InlineKeyboardButton();
        reviewButton.setText("Пропозиція");
        reviewButton.setUrl("https://forms.gle/eMp8FyZ3VmKhrfpB9");

        rowInline.add(reviewButton);
        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        executeMessage(message);

    }

    public void donateLink(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("<b>\uD83D\uDCB0Відправте донат на каву розробнику." + "\n" + "✅ 10% буде відправлено на ЗСУ\uD83C\uDDFA\uD83C\uDDE6." + "\n\n" + "\uD83D\uDCCCДіліться ботом з друзями, щоб допомогти вивчити англійську</b>");
        message.setParseMode(ParseMode.HTML);
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        var donateButton = new InlineKeyboardButton();
        donateButton.setText("Відправити донат");
        donateButton.setUrl("https://send.monobank.ua/jar/32qKR5ZGmm");

        rowInline.add(donateButton);
        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        executeMessage(message);
    }

    public void registerUser(Message msg) {
        if (userRepository.findById(msg.getChatId()).isEmpty()) {

            User user = new User();
            user.setChatId(msg.getChatId());
            user.setFirstName(msg.getChat().getFirstName());
            user.setLastName(msg.getChat().getLastName());
            user.setUserName(msg.getChat().getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
        }

    }


    public void sendVerbsGroup(List<?> group, long chatId) {
        StringBuffer sendVerbsGroup = new StringBuffer();
//        StringBuilder sendVerbsGroup = new StringBuilder();
        for (Object object : group) {
            if (object instanceof VerbGroup1) {
                VerbGroup1 verbGroup1 = (VerbGroup1) object;
                String temp = "\uD83D\uDCCC " + verbGroup1.getInfinitive() + "\n" + "\uD83D\uDCCC " + verbGroup1.getPastIndefinite() + "\n" + "\uD83D\uDCCC " + verbGroup1.getPastParticiple() + "\n" + "✅ " + verbGroup1.getTranslate() + "\n\n";
                sendVerbsGroup.append(temp);

            } else if (object instanceof VerbGroup2) {
                VerbGroup2 verbGroup2 = (VerbGroup2) object;
                String temp = "\uD83D\uDCCC " + verbGroup2.getInfinitive() + "\n" + "\uD83D\uDCCC " + verbGroup2.getPastIndefinite() + "\n" + "\uD83D\uDCCC " + verbGroup2.getPastParticiple() + "\n" + "✅ " + verbGroup2.getTranslate() + "\n\n";
                sendVerbsGroup.append(temp);

            } else if (object instanceof VerbGroup3) {
                VerbGroup3 verbGroup3 = (VerbGroup3) object;
                String temp = "\uD83D\uDCCC " + verbGroup3.getInfinitive() + "\n" + "\uD83D\uDCCC " + verbGroup3.getPastIndefinite() + "\n" + "\uD83D\uDCCC " + verbGroup3.getPastParticiple() + "\n" + "✅ " + verbGroup3.getTranslate() + "\n\n";
                sendVerbsGroup.append(temp);
            } else if (object instanceof VerbGroup4) {
                VerbGroup4 verbGroup4 = (VerbGroup4) object;
                String temp = "\uD83D\uDCCC " + verbGroup4.getInfinitive() + "\n" + "\uD83D\uDCCC " + verbGroup4.getPastIndefinite() + "\n" + "\uD83D\uDCCC " + verbGroup4.getPastParticiple() + "\n" + "✅ " + verbGroup4.getTranslate() + "\n\n";
                sendVerbsGroup.append(temp);
            } else if (object instanceof VerbGroup5) {
                VerbGroup5 verbGroup5 = (VerbGroup5) object;
                String temp = "\uD83D\uDCCC " + verbGroup5.getInfinitive() + "\n" + "\uD83D\uDCCC " + verbGroup5.getPastIndefinite() + "\n" + "\uD83D\uDCCC " + verbGroup5.getPastParticiple() + "\n" + "✅ " + verbGroup5.getTranslate() + "\n\n";
                sendVerbsGroup.append(temp);
            } else if (object instanceof VerbGroup6) {
                VerbGroup6 verbGroup6 = (VerbGroup6) object;
                String temp = "\uD83D\uDCCC " + verbGroup6.getInfinitive() + "\n" + "\uD83D\uDCCC " + verbGroup6.getPastIndefinite() + "\n" + "\uD83D\uDCCC " + verbGroup6.getPastParticiple() + "\n" + "✅ " + verbGroup6.getTranslate() + "\n\n";
                sendVerbsGroup.append(temp);
            }
        }
        sendMessage(chatId, sendVerbsGroup.toString());

    }

    public void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);


    }

    public void deleteMessage(long chatId, Integer messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            loggerError.error(ERROR_TEXT + e.getMessage());
        }

    }


    public void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            loggerError.error(ERROR_TEXT + e.getMessage());
        }
    }

    public boolean isEnglishText(String text) {
        return text.matches("[A-Za-z]+");
    }

}

