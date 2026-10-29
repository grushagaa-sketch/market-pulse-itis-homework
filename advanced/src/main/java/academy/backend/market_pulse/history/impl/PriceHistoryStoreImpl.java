package academy.backend.market_pulse.history.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import academy.backend.market_pulse.history.PriceHistoryStore;
import academy.backend.market_pulse.history.PricePoint;

/**
 * Заготовка для самостоятельной работы (см. «Требования к файловому хранилищу.md», семинар 7) —
 * структура есть, решения нет. Формат файла — на ваш выбор (бинарный, текстовый и т.д.); главное,
 * чтобы каждый {@link #append} создавал НОВЫЙ файл (не переписывал предыдущий) и
 * {@link #load(Path, String)} возвращал все сохранённые точки по тикеру в порядке возрастания
 * времени. Проверьте себя тестом {@code PriceHistoryStoreContractTest} (замените в нём эталонную
 * реализацию на свою).
 */
public class PriceHistoryStoreImpl implements PriceHistoryStore {

    @Override
    public Path append(Path directory, PricePoint point) {
        try {
            if (directory != null) {
                Files.createDirectories(directory);
            }

            String fileName = point.ticker() + "_" + point.timestamp().toString().replace(":", "-") + ".txt";
            Path filePath = directory.resolve(fileName);

            String content = point.timestamp() + ";" + point.price();
            Files.writeString(filePath, content);

            return filePath;
        } catch (IOException e) {
            throw new UncheckedIOException("Ошибка при добавлении точки в историю", e);
        }
        // TODO: записать точку в новый файл внутри directory — существующие файлы не трогать.
        //  Подсказка: имя файла на основе point.timestamp() даёт сортировку по имени = сортировку
        //  по времени.
    }

    @Override
    public List<PricePoint> load(Path directory, String ticker) {
        if (!Files.exists(directory)) {
            return new ArrayList<>();
        }

        try (Stream<Path> files = Files.list(directory)) {
            List<PricePoint> points = new ArrayList<>();

            files.filter(p -> p.getFileName().toString().startsWith(ticker + "_"))
                    .forEach(p -> {
                        try {
                            String[] parts = Files.readString(p).split(";");
                            if (parts.length == 2) {
                                Instant time = Instant.parse(parts[0]);
                                BigDecimal price = new BigDecimal(parts[1]);
                                points.add(new PricePoint(ticker, time, price));
                            }
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });

            points.sort(Comparator.comparing(PricePoint::timestamp));
            return points;

        } catch (IOException e) {
            throw new UncheckedIOException("Ошибка при чтении истории", e);
        }
        // TODO: прочитать все точки по тикеру в порядке возрастания времени.
        //  Если для тикера ещё нет точек — вернуть пустой список, а не бросать исключение.
    }
}
