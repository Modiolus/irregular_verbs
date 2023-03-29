package com.english.irregular_verbs.service;

import com.english.irregular_verbs.config.BotConfig;
import com.english.irregular_verbs.model.IrregularVerb;
import com.english.irregular_verbs.model.User;
import com.english.irregular_verbs.repositories.IrregularVerbRepository;
import com.english.irregular_verbs.repositories.UserRepository;
import com.vdurmont.emoji.EmojiParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private static final Logger loggerError = LoggerFactory.getLogger("logger.error");
    static final String ERROR_TEXT = "Error occurred: ";
    private IrregularVerbRepository irregularVerbRepository;
    private UserRepository userRepository;
    private BotConfig config;
    private EntityManager entityManager;

    @Autowired
    public TelegramBot(IrregularVerbRepository irregularVerbRepository, UserRepository userRepository, BotConfig config,
                       EntityManager entityManager) {
        this.irregularVerbRepository = irregularVerbRepository;
        this.userRepository = userRepository;
        this.config = config;
        this.entityManager = entityManager;

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

        if (update.hasMessage() && update.getMessage().hasText()) {

            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            var verbs = irregularVerbRepository.findAll();
            var users = userRepository.findAll();

            searchVerb(verbs, messageText, chatId);
//            String[] ex = (String[]) verbs.stream()
//                    .filter(element -> element.getPastIndefinite().split("[\\s,]+").equals(messageText.toLowerCase()))
//                    .toArray();
//
//                switch (messageText) {
//                    case "/start":
//                        registerUser(update.getMessage());
//                        startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
//                        break;
////                        default:
////                            prepareAndSendMessage(chatId, "Sorry, command was not recognized");
//                }
//            }
//        }


        }

    }

    private void searchVerb(List<IrregularVerb> verbs, String messageText, long chatId) {
        boolean flag = false;
        for (IrregularVerb verb : verbs) {

            String[] infinitiveArray = verb.getInfinitive().split(" ");
            String[] pastIndefiniteArray = verb.getPastIndefinite().split("[\\s,]+");
            String[] pastParticipleArray = verb.getPastParticiple().split("[\\s,]+");

            if (messageText.toLowerCase().equals(infinitiveArray[0])
                    || messageText.toLowerCase().equals(pastIndefiniteArray[0])
                    || messageText.toLowerCase().equals(pastIndefiniteArray[pastIndefiniteArray.length - 2])
                    || messageText.toLowerCase().equals(pastParticipleArray[0])
                    || messageText.toLowerCase().equals(pastParticipleArray[pastParticipleArray.length - 2])) {
                flag = true;
                prepareAndSendMessage(chatId, "\uD83D\uDCCC " + verb.getInfinitive() + "\n"
                        + "\uD83D\uDCCC " + verb.getPastIndefinite() + "\n" + "\uD83D\uDCCC " + verb.getPastParticiple() + "\n"
                        + "✅ " + verb.getTranslate());
            }

        }
        if (!flag) {
            prepareAndSendMessage(chatId, "❗Sorry, this verb isn't irregular");
        }


    }

    private void registerUser(Message msg) {
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

    private void prepareAndSendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();

        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Hi " + name + ", nice to meet you!" + " :blush:");
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);

    }


    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            loggerError.error(ERROR_TEXT + e.getMessage());
        }
    }

}

