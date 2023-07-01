# Zadanie 2

## Zabezpieczenie

Aplikacja została zabezpieczona przy użyciu Spring Security.
Wykorzystane zostały JSON Web Tokeny.
Zaimplementowany został również system refresh tokenów, które odświeżają access token.
Refresh token powinien znajdować się w ciasteczku w chwili wysyłania requesta do endpointa z odświeżaniem tokena.
Rejestracja znajduje się na endpointcie "/api/auth/register", a logowanie na "/api/auth/login".
Kontroler, serwis, klasy response i request znajdują się w pakiecie auth.
Filtr, przez który przechodzą requesty do api, znajduje się w pakiecie config.
Znajdują się tam również pliki odpowiedzialne za konfigurację zabezpieczeń, serwis access tokena.

Pliki odpowiedzialne za zarządzanie refresh tokenem znajdują się w pakiecie refreshToken.
W kontrolerze są endpointy odpowiadające za wysyłanie refresh tokena i za wylogowanie (niszczenie refresh tokena).

Użytkownicy mają role: ADMIN i USER. User jest w stanie wysyłać i odczytywać swoje dane, a Admin może dodatkowo
wyświetlać wszystkie dane.

## Zbieranie lokalizacji

Utworzone zostały encje Device i Location. Do każdego urządzenia przypisany zostaje użytkownik User.
Jeżeli do urządzenia zostanie przypisany użytkownik, inny użytkownik nie może z niego korzystać.
Zostały nałożone ograniczenia do latitude (wartość <-180, 180>) i longitude (wartość <-90, 90>)
Endpointy:

- POST "api/location" - odpowiada za przesyłanie danych do API
- GET "api/location?pageNum=0&pageSize=10" - odpowiada za odbieranie wszystkich informacji o lokacji.
  PageNum pozwala na wybranie strony, a pageSize za liczbę przesłanych rekordów na stronę.
- GET "api/location/user?pageNum=0&pageSize=10" - odpowiada za odbieranie danych wysłanych przez użytkownika.

## Testy

W aplikacji zostały przetestowane repozytoria, serwisy i kontrolery.
Do testów została wykorzystana baza H2.