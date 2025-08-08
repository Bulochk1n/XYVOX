package exchange.xyvox.Services;

import exchange.xyvox.Models.Enums.FuturesPositionStatusEnum;
import exchange.xyvox.Models.Enums.OrderStatusEnum;
import exchange.xyvox.Models.FuturesOrder;
import exchange.xyvox.Models.FuturesPosition;
import exchange.xyvox.Repositories.FuturesPositionRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class FuturesPositionService {
    private final FuturesPositionRepository futuresPositionRepository;
    private final WalletService walletService;
    private final CoinService coinService;

    public FuturesPositionService(FuturesPositionRepository futuresPositionRepository, WalletService walletService, CoinService coinService) {
        this.futuresPositionRepository = futuresPositionRepository;
        this.walletService = walletService;
        this.coinService = coinService;
    }


    public FuturesPosition findFuturesPositionById(Integer id) {
        return futuresPositionRepository.findById(id).orElse(null);
    }

    public void updateFuturesPositionStatus(Integer futuresPositionId, FuturesPositionStatusEnum status) {
        FuturesPosition futuresPosition = findFuturesPositionById(futuresPositionId);
        futuresPosition.setStatus(status);
        futuresPositionRepository.save(futuresPosition);
        walletService.updateFuturesPosition(futuresPosition.getFuturesOrder().getWallet().getId(), futuresPosition);
    }

    public FuturesPosition closeFuturesPosition(Integer futuresPositionId) {
        FuturesPosition futuresPosition = findFuturesPositionById(futuresPositionId);
        futuresPosition.setExitPrice(futuresPosition.getFuturesOrder().getCoin().getPrice());

        futuresPosition.setStatus(FuturesPositionStatusEnum.CLOSED);
        futuresPosition.setClosedAt(new Timestamp(System.currentTimeMillis()));

        Double pnl = (futuresPosition.getExitPrice() - futuresPosition.getEntryPrice()) * futuresPosition.getAmount() * futuresPosition.getLeverage();
        System.out.println("pnl = " + pnl);
        futuresPosition.setPnl(pnl);

        Double returnMoney = futuresPosition.getProvidedValue() + pnl;
        System.out.println("returnMoney = " + returnMoney);
        walletService.updateCoinAmount(futuresPosition.getWallet().getId(), coinService.getCoinBySymbol("USDT"), returnMoney, "buy", futuresPosition.getTransaction().getId());


        return futuresPositionRepository.save(futuresPosition);
    }

}
