# Война Вирусов

Война Вирусов — игра, в которую играют два игрока на поле 10 на 10.
Цель игры — захватить как можно больше клеток, устанавливая новые свои клетки или уничтожая клетки соперника.
В рамках этого проекта будет реализовано приложение для игры в Войну Вирусов по сети против другого игрока,
а также, возможно, офлайн-игра против бота.

Приложение будет состоять из серверной и клиентской части, сообщающихся через [Google Cloud Messaging](https://developers.google.com/cloud-messaging/).
Серверная часть отвечает за хранение всех данных, в том числе текущих игр, и пересылку ходов игрокам.
Клиентская часть предлогает игроку войти, настраивать свой аккаунт, выбрать противника для игры и, собственно, играть.

# Данные

Необходимо хранить в базе данных следующее:

## Игрок

Сущность, описывающая игрока.

1. Авторизация через аккаунт [Google](https://developers.google.com/identity/sign-in/android/) — храним токен авторизации.
1. Ник — заданный пользователем ник плюс номер для уникальности (как в Battle.net).
1. Цвет — пользователь может выбрать цвет его фигурок.
1. Статус высылаемого приглашения — его время и цель. Если не было отвечено, за сутки сбрасывается.
1. Последний момент онлайна (активности).

## Девайсы

Отображение один пользователь — многие индетификаторы его устройств.
Используются для рассылки уведомлений.

## Контакты

Отобржаение один пользователь — многие пользователи, которые добавлены в контакты.
Это отображение не обязательно симметричное.

В качестве помощи в поиске друзей можно испольовать [Google+ API](https://developers.google.com/+/mobile/android/people).

## Сохранённые игры

1. Номер игры
1. Игроки (кто играл крестиками и ноликами)
1. Результат (кто победил)

Ходы в игре хранятся в отдельной таблице.

## Ходы

Отображение номер игры и номер хожа — ход.
Хранит действия игроков для последующего просмотра, статистики и прочего.

# Сервер

1. Подключается к GCM.
1. Хранит состояния открытых игр.
1. Для входа и выхода пользователей ждёт их команд через POST-запросы.

# Протокол

1. Авторизация
	1. HTTP-запросы на вход.
		Требуется проверить токен через Google API, сохранить токен девайса.
		Возможно, аккаунт новый.

1. Информация о аккаунте
    1. Общие настройки — получение, отправка.
    1. Список друзей — получение, отправка действий (добавление, удаление).

1. Приглашения
	1. Пригласить в игру, отменить приглашение
	1. Получить приглашение
	1. Ответ на приглашение: согласие и запуск игры, отказ

1. Игра
    1. Ходики, в том числе пропуск хода.
    1. Сдаться.

1. Сообщения?

# Клиент

## Activity:

1. Main (список друзей, переходы настройки, список игр и так далее).
	При запуске — если есть открытая игра, сразу перейти в игру.
	Активити отображает список контактов для выбора противника.
	Первым идёт бот.
	Далее идёт список контактов (если пользователь зашёл) или кнопка входа (в ином случае).
	При нажатии отображает аккаунт игрока.
	Список обновляется при нажатии кнопки или если его потянуть вниз.
	У активити есть шторка — оттуда можно перейти в настройки, в свой аккаунт, выйти из аккаунта.

1. Settings — панель настроек.
	Предлагается список настроек. Есть выход из аккаунта, ник игрока, его цвет, звук, уведомления.

1. Game — отображает уже прошедшие игры или текущую.
	Получает, что делать (воспроизведение уже прошедшей игры, запуск игры с заданным противником).
    Отображает заголовок с никами, кто за кого играет.
    Также в центре игровое поле 10*10, при игре пользователь нажимает клетки для хода.
	В клетке отображается символ, кому принадлежит клетка: пустая, кретик, нолик, обведённый крестик и закрашенный нолик.
	Цвет клетки соотвествует владельцу клетки.
    Поле также отображает возможные клетки для хода, подсвечивая их границу.
	Также отображаются кнопки "Пропустить/Закончить ход", "Сдаться".
    При отображении сохранённой игры отображаются кнопки перемотки:
    вперёд, назад, то же самое до конца, посередине число ходов и номер текущего хода.

1. Аккаунт игрока.
    Отображает общую информацию об игроке (ник, цвет, статистика, онлайн ли).
    Также отображает список сохранённых игр (при нажатии открывает сохранённую игру),
    кнопка играть (для себя не отображается).

# Код

## Классы протокола
1. Методы постоения и разбора сообщений.
1. Диспатчеры, обрабатывающие сообщения (как клиентские, так и серверные).
1. Инициализация-деинициализация GCM.
1. Генерация уведомлений (клиент).

## Класс игры
Так храним открытые игры (не завершённые на сервере, а также всё, что отображается на клиенте).
Сохранённые, закрытые игры хранятся в базе данных.

1. Поле
1. Определение очереди хода
1. Предоставление корректных для хода клеток
1. Проверка ходов
1. Проверка окончания игры
1. Прочая игровая логика
	
## Интерфейс участника игры.
Отвественнен за обработку хода одного из игроков.
Передаётся в Activity игры для взаимодействия с противником.

### Пользователь
Предлагает пользователю походить на Activity.

### Противник-игрок
Сообщает о ходе игрока по сети противнику, ждёт ответа.

### Противник-бот
Думает ходы локально перебором с отсечениями.