### Описание

Это программа для моделирования поведения систем магнитных моментов в магнитных полях.

Она состоит из двух частей -- собственно модель (на Kotlin), в которой происходит моделирование системы, 
и координатор (на Python), который занимается запуском модели с различными параметрами, а также рисованием графиков
данных, полученных в результате моделирования.  
 
### Установка

Для развертывания проекта, необходимо:
 1. клонировать из репозитория
 2. инициализировать проект с помощью *Gradle*, конфигурационный файл -- `gradlew init` 
 3. собрать используемый проектом интерпретатор питона: `gradlew build_envs`

### Запуск

Для запуска необходимо запустить основной скрипт с помощью `gradlew create_and_draw`. Настройки будут браться из файла
`./resources/settings.json`.

### Настройки модели:

```javascript
{
     "x":1, // количество клеток по x
     "y":1, // количество клеток по y
     "z":1, // количество клеток по z
     "n":50, // число частиц в кольце

     "r":1.25, // радиус частицы
     "d":60.0, // диаметр кольца [нм]
     "offset_x":4.0, // расстояние между клетками  по оси x [нм]
     "offset_y":4.0, // расстояние между клетками  по оси y [нм]
     "offset_z":4.0, // расстояние между клетками  по оси z [нм]

     "m":800.0, // значение момента [магнетон бора, шт]
     "kan":8000.0, // константа анизотропии [Дж/м^3]
     "jex":5.0e0, // константа обмена [Тл^2 / эВ]

     // расстояния, на которых чувтствуются взаимодействия:
     "dipolDistance":30.0, // диполь-дипольное, [нм]
     "exchangeDistance":4.0, // обменное, [нм]

     "viscosity":0.9, // коэффициент вязкости, 0 <= viscosity <= 1
     "t":0.0, // температура [К]

     "loc":0, // начальное расположение моментов
              // 0 -- рандом в 3D, 1 -- рандом в 2D, 2 -- заданная ось
              // отклонение оси анизотропии от оси z и оси x соответственно
     "loc_theta":90.0, // [градус]
     "loc_phi":0.0, // [градус]

     "ot":0, // расположение осей анизотропии (аналогично loc)
     "ot_theta":90.0, // [градус]
     "ot_phi":0.0, // [градус]

     "b_x":0.0, // поле по x [Тл]
     "b_y":0.0, // поле по y [Тл]
     "b_z":0.0, // поле по z [Тл]

     "time":1.0, // время релаксации [с]
     "timeStep":100, // временной шаг [нс]

     "name":"default", // имя модели (используется для графиков и логов)

     "precision":7, // точность (количество шагов симуляции)
     "load":false, // загружать ли предыдущее состояние
     "jsonPath":"./resources/data/default/out/sample.json", // путь к сохраненному состоянию

     "hysteresis":false, // нужно ли запускать в режиме гистерезиса
     "hysteresisSteps":7, // количество шагов гистерезиса в ветке от нуля до края
     "hysteresisDenseSteps":2, // количество больших шагов в частой области
     "hysteresisDenseMultiplier":2, // отношение шага в обычной области к шагу в частой области

     "is2dPlot":true, // рисовать трехмерные графики или двумерные
     "xAxis":"x", // координата по оси абсциис (2D график)
     "yAxis":"y", // координата по оси ординат (2D график)

     "dataFolder":"./resources/data", // путь к папке для выходных данных

     "isParallel":false, // использовать ли параллельные вычисления
     "memory":2048 // количество памяти, выделяемой для java-машины
}
```