package service;

import java.util.ArrayList;
import entity.Card;
import entity.Station;
import exceptions.*;
import persistence.*;

public class MetroSystemServiceImpl implements MetroSystemService {
    CardDao card = new CardDaoImpl();
    StationDao station = new StationDaoImpl();

    @Override
    public ArrayList<Station> getStationList() {
        return station.getStationList();
    }

    @Override
    public void insertTransactionRecord() {
        // TODO: Implement transaction logging
    }

    @Override
    public int issueCard(double amount) throws LowAmountException {
        return card.issueCard(amount);
    }

    @Override
    public Card getCardDetailsById(int id) throws WrongCardNoException {
        return card.getCardDetailsById(id);
    }

    @Override
    public void checkCardExist(int cardId) throws WrongCardNoException {
        card.checkCardExist(cardId);
    }

    @Override
    public void checkStationExist(int stationId) throws MetroStationDoNotExistException {
        station.checkStationExist(stationId);
    }

    @Override
    public void swipeIn(int cardId, int stationId) throws InsufficientBalanceException {
        card.swipeIn(cardId, stationId);
    }

    @Override
    public void addCardBalance(int cardId, double amount) {
        card.addCardBalance(cardId, amount);
    }

    // Fixed `swipeOut` method
    @Override
    public double swipeOut(int cardId, int stationId) throws NotSwipedInException, InsufficientBalanceException, WrongCardNoException, MetroStationDoNotExistException {
        checkCardExist(cardId);
        checkStationExist(stationId);

        // Get card details
        Card userCard = getCardDetailsById(cardId);

        // Check if the card has a valid swipe-in record
        if (!userCard.isSwipedIn()) {
            throw new NotSwipedInException("Swipe-in record not found! Please swipe in before swiping out.");
        }

        int startStation = userCard.getLastSwipeInStation();
        if (startStation == -1) {
            throw new NotSwipedInException("Invalid swipe-in station.");
        }

        int distance = Math.abs(stationId - startStation);
        double fare = distance * 5; 

        // Check if balance is sufficient
        if (userCard.getBalance() < fare) {
            throw new InsufficientBalanceException("Insufficient balance to deduct fare.");
        }

        // Deduct fare and update card balance
        userCard.setBalance(userCard.getBalance() - fare);
        userCard.setSwipedIn(false); // Reset swipe-in status

        // Log transaction
        insertTransactionRecord();

        return fare;
    }
}
