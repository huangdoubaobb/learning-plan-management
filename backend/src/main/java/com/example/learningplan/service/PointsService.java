package com.example.learningplan.service;

import com.example.learningplan.entity.PointsLedger;
import com.example.learningplan.entity.PointsRefType;
import com.example.learningplan.entity.PointsType;
import com.example.learningplan.entity.User;
import com.example.learningplan.repository.PointsLedgerRepository;
import com.example.learningplan.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PointsService {
    private final PointsLedgerRepository pointsLedgerRepository;
    private final UserRepository userRepository;

    public PointsService(PointsLedgerRepository pointsLedgerRepository, UserRepository userRepository) {
        this.pointsLedgerRepository = pointsLedgerRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void earnPoints(User child, int points, PointsRefType refType, Long refId, String note) {
        if (points <= 0) {
            return;
        }
        child.setPoints(child.getPoints() + points);
        userRepository.save(child);

        PointsLedger ledger = new PointsLedger();
        ledger.setChild(child);
        ledger.setPoints(points);
        ledger.setType(PointsType.EARN);
        ledger.setRefType(refType);
        ledger.setRefId(refId);
        ledger.setNote(note);
        pointsLedgerRepository.save(ledger);
    }

    @Transactional
    public void spendPoints(User child, int points, PointsRefType refType, Long refId, String note) {
        if (points <= 0) {
            return;
        }
        if (child.getPoints() < points) {
            throw new IllegalStateException("积分不足");
        }
        child.setPoints(child.getPoints() - points);
        userRepository.save(child);

        PointsLedger ledger = new PointsLedger();
        ledger.setChild(child);
        ledger.setPoints(points);
        ledger.setType(PointsType.SPEND);
        ledger.setRefType(refType);
        ledger.setRefId(refId);
        ledger.setNote(note);
        pointsLedgerRepository.save(ledger);
    }
}