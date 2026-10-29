package academy.backend.market_pulse.repository.store.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import academy.backend.market_pulse.factory.InstrumentFactories;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.repository.store.QuoteFileStore;

/**
 * Заготовка для самостоятельной работы (см. «Требования к файловому хранилищу.md», семинар 7) —
 * структура есть, решения нет. Формат файла — на ваш выбор (JSON, CSV, построчный текст); главное,
 * чтобы {@code load(save(quotes))} возвращал те же котировки. Проверьте себя тестом
 * {@code QuoteFileStoreContractTest} (замените в нём эталонную реализацию на свою).
 */
public class QuoteFileStoreImpl implements QuoteFileStore {

    @Override
    public void save(Path path, List<Quote> quotes) {
        List<String> lines = new ArrayList<>();
        for (Quote q : quotes) {
            Instrument inst = q.getInstrument();
            lines.add(String.join(";",
                    inst.getType(),
                    inst.getTicker(),
                    inst.getName(),
                    inst.getCurrency().name(),
                    q.getPrice().toString(),
                    q.getChangePercent().toString()
            ));
        }
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.write(path, lines);
        } catch (IOException e) {
            throw new UncheckedIOException("Ошибка при сохранении котировок", e);
        }
        // TODO: сохранить котировки в файл по указанному пути.
        //  Подсказка: на каждую котировку нужно сохранить тикер, тип и валюту инструмента (чтобы
        //  потом восстановить его через InstrumentFactories.create), а также цену и изменение за
        //  период — этого достаточно, чтобы собрать Quote обратно.
    }

    @Override
    public List<Quote> load(Path path) {
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try {
            List<String> lines = Files.readAllLines(path);
            List<Quote> quotes = new ArrayList<>();
            for (String line : lines) {
                if (line.isEmpty()) continue;
                String[] parts = line.split(";");
                Instrument inst = InstrumentFactories.create(
                        parts[0], // type
                        parts[1], // ticker
                        parts[2], // name
                        Currency.valueOf(parts[3])
                );
                BigDecimal price = new BigDecimal(parts[4]);
                BigDecimal change = new BigDecimal(parts[5]);
                quotes.add(new Quote(inst, price, change));
            }
            return quotes;
        } catch (IOException e) {
            throw new UncheckedIOException("Ошибка при чтении котировок", e);
        }
        // TODO: прочитать котировки из файла и восстановить при старте.
        //  Если файла ещё нет (первый запуск) — вернуть пустой список, а не бросать исключение.
    }
}
