package com.english.irregular_verbs.service;

import com.english.irregular_verbs.config.BotConfig;
import com.english.irregular_verbs.model.IrregularVerb;
import com.english.irregular_verbs.model.User;
import com.english.irregular_verbs.repositories.IrregularVerbRepository;
import com.english.irregular_verbs.repositories.UserRepository;
import com.vdurmont.emoji.EmojiParser;;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.*;

@Component
public class TelegramBot extends TelegramLongPollingBot implements BotVerbs {

    //    static final String REVIEW_BUTTON = "REVIEW_BUTTON";
    private static final Logger loggerError = LoggerFactory.getLogger("logger.error");
    private static final Logger loggerInfo = LoggerFactory.getLogger("logger.info");
    static final String ERROR_TEXT = "Error occurred: ";
    private final IrregularVerbRepository irregularVerbRepository;
    private final UserRepository userRepository;
    private final BotConfig config;
    List<BotCommand> listOfCommands = new ArrayList<>();

    @Autowired
    public TelegramBot(IrregularVerbRepository irregularVerbRepository, UserRepository userRepository, BotConfig config) {
        this.irregularVerbRepository = irregularVerbRepository;
        this.userRepository = userRepository;
        this.config = config;

        listOfCommands.add(new BotCommand("/start", "Почати роботу"));
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

        if (update.hasMessage() && update.getMessage().isCommand()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            String text = message.getText();
            if (text.equals(listOfCommands.get(0).getCommand())) {
                registerUser(message);
                loggerInfo.info("User " + update.getMessage().getChat().getFirstName() + " push the " + listOfCommands.get(0));
                sendMessage(chatId, "Hi " + update.getMessage().getChat().getFirstName() + ", nice to meet you ☺");
            } else if (text.equals(listOfCommands.get(1).getCommand())) {
                suggestionLink(chatId);
                loggerInfo.info("User " + update.getMessage().getChat().getFirstName() + " push the " + listOfCommands.get(1));
            } else if (text.equals(listOfCommands.get(2).getCommand())) {
                donateLink(chatId);
                loggerInfo.info("User " + update.getMessage().getChat().getFirstName() + " push the " + listOfCommands.get(2));

            } else if (text.contains("/send") && config.getOwnerId() == chatId) {
                var textToSend = EmojiParser.parseToUnicode(text.substring(text.indexOf(" ")));
                var users = userRepository.findAll();
                for (User user : users) {
                    sendMessage(user.getChatId(), textToSend);
                    loggerInfo.info("Message: " + textToSend + " sent from the bot's owner");
                }

            }

        } else {

            Message message = update.getMessage();
            long chatId = message.getChatId();
            String messageText = message.getText();
            var verbs = irregularVerbRepository.findAll();

            searchVerb(verbs, messageText, chatId);
            loggerInfo.info("User " + update.getMessage().getChat().getFirstName() + " found word: " + messageText);
        }

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
        message.setText("<b>\uD83D\uDCB0Відправте донат на каву розробнику."
                + "\n" + "✅ 10% буде відправлено на ЗСУ\uD83C\uDDFA\uD83C\uDDE6."
                + "\n\n" + "\uD83D\uDCCCДіліться ботом з друзями, щоб допомогти вивчити англійську</b>");
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

    public void searchVerb(List<IrregularVerb> verbs, String messageText, long chatId) {
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
                sendMessage(chatId, "\uD83D\uDCCC " + verb.getInfinitive() + "\n"
                        + "\uD83D\uDCCC " + verb.getPastIndefinite() + "\n" + "\uD83D\uDCCC " + verb.getPastParticiple() + "\n"
                        + "✅ " + verb.getTranslate());
            }

        }
        if (!flag) {
            sendMessage(chatId, "❗Вибачте, це дієслово не є неправильним");
        }


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

    public void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);

    }


    public void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            loggerError.error(ERROR_TEXT + e.getMessage());
        }
    }

}

