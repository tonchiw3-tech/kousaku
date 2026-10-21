# KOSAKU - 香りづくり体験予約管理システム

KOSAKUは、浅草の香りづくり体験を対象としたWeb予約管理アプリケーションです。WEB制作総合演習1で制作した「旅香 - TABIKA -」と連携する予約システムとして、WEB制作総合演習2で開発しました。

利用者はWeb上でワークショップを予約・問い合わせでき、スタッフは予約内容や対応状況を管理できます。

## 主な機能

### 利用者向け

- ワークショップの予約登録
- 予約番号の自動発行、予約金額の自動計算
- お問い合わせの登録
- 問い合わせ番号による返信状況の確認

### スタッフ向け

- スタッフログイン
- 予約一覧・詳細の確認
- 予約ステータスの変更、キャンセル、削除
- 完了済み予約の訂正と訂正履歴の管理
- 月間予約カレンダー、日別予約・準備状況の確認
- お問い合わせの対応状況・返信内容の管理

### 予約ステータス

| ステータス | 内容 |
| --- | --- |
| `RECEIVED` | 予約受付 |
| `CONFIRMED` | 予約確認 |
| `READY` | 準備完了 |
| `COMPLETED` | 体験完了 |
| `CANCELED` | キャンセル |

## 使用技術

| 分類 | 技術 |
| --- | --- |
| 言語 | Java 21 |
| フレームワーク | Spring Boot 4.0.8 |
| データアクセス | MyBatis 4.0.1 |
| データベース | MySQL 8.0 |
| 画面テンプレート | Thymeleaf |
| フロントエンド | HTML / CSS / JavaScript |
| 認証・認可 | Spring Security |
| テスト | JUnit 5 / Spring Boot Test / MockMvc |
| コンテナ | Docker / Docker Compose |

## システム構成

```text
利用者・スタッフ
       ↓
   Webブラウザ
       ↓
  Spring Boot
       ↓
Controller → Service → MyBatis Mapper → MySQL
```

予約・問い合わせのリクエストをSpring Bootで処理し、MyBatisを介してMySQLに保存します。

## データベース構成

| テーブル名 | 用途 |
| --- | --- |
| `reservations` | 予約情報 |
| `reservation_status_corrections` | 予約ステータスの訂正履歴 |
| `inquiries` | 問い合わせ・返信状況 |

詳細な定義は [schema.sql](src/main/resources/schema.sql) を参照してください。

## 主なAPI

| 機能 | メソッド | エンドポイント | 権限 |
| --- | --- | --- | --- |
| 予約登録 | `POST` | `/api/reservations` | 公開 |
| 問い合わせ登録 | `POST` | `/api/inquiries` | 公開 |
| 問い合わせ状況確認 | `GET` | `/api/inquiries/status?inquiryNumber={番号}` | 公開 |
| 予約管理 | `GET` / `PATCH` / `DELETE` | `/api/staff/reservations/**` | スタッフ |
| 予約カレンダー | `GET` | `/api/staff/calendar` | スタッフ |
| 問い合わせ管理 | `GET` / `PATCH` | `/api/staff/inquiries/**` | スタッフ |

スタッフ用APIおよび画面は、Spring Securityで`STAFF`ロールに制限しています。

## ローカル環境での起動

### 前提条件

- Java 21
- Docker Desktop
- Git

### 1. リポジトリを取得

```bash
git clone https://github.com/tonchiw3-tech/kousaku.git
cd kousaku
```

### 2. 環境変数を設定

`.env.example`を参考に、プロジェクト直下に`.env`を作成します。パスワードなどの機密情報はGitへコミットしないでください。

```dotenv
DB_URL=jdbc:mysql://localhost:3306/kousaku?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Tokyo
DB_USERNAME=your_mysql_user
DB_PASSWORD=your_mysql_password
STAFF_USERNAME=staff_user
STAFF_PASSWORD=staff_password

MYSQL_DATABASE=kousaku
MYSQL_USER=your_mysql_user
MYSQL_PASSWORD=your_mysql_password
MYSQL_ROOT_PASSWORD=your_mysql_root_password
```

`DB_USERNAME` / `DB_PASSWORD` は、Docker Composeの `MYSQL_USER` / `MYSQL_PASSWORD` と同じ値を設定してください。

### 3. データベースを起動

```bash
docker compose up -d
```

アプリケーション起動時に `schema.sql` が実行され、必要なテーブルが作成されます。

### 4. Spring Bootを起動

Windowsの場合:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS / Linuxの場合:

```bash
./mvnw spring-boot:run
```

### 5. ブラウザで確認

- 利用者向け予約画面: <http://localhost:8080>
- スタッフ用予約管理画面: <http://localhost:8080/staff/reservations>

## テスト

JUnitによる単体テスト・結合テストを用意しています。

```powershell
.\mvnw.cmd test
```

テストでは専用データベース `kousaku_test` を使用します。テスト用接続設定は [src/test/resources/application.properties](src/test/resources/application.properties) を参照してください。

### 最新テスト実行結果

- JUnitテスト19件、成功19件、失敗0件、エラー0件、スキップ0件
- BUILD SUCCESS

### 実装上の注意

- 祝日判定は未実装です。予約可能日の判定は土曜日・日曜日を対象としています。

## 関連プロジェクト・設計資料

WEB制作総合演習1・2の設計資料は、別リポジトリ「旅香 - TABIKA -」で管理しています。

- [旅香 - TABIKA - リポジトリ](https://github.com/tonchiw3-tech/washizu_2026.09.10)
- 制作資料: 企画提案書 / マーケットリサーチ / ペルソナ / サイトマップ / ワイヤーフレーム / デザインガイドライン / 仕様書 / DB設計書 / テスト報告書

## デプロイ

Dockerfileと`.dockerignore`を用意し、Renderへの公開に対応する構成です。本番用MySQLの接続設定と公開動作確認は進行中です。

## 制作について

本アプリケーションは、職業訓練の「WEB制作総合演習2」における卒業制作です。演習1で作成した企画・設計資料をもとに、AI開発支援ツールも活用しながら、Java / Spring Bootによるバックエンド実装、MySQLによるデータ管理、JUnitによるテストに取り組んでいます。

- 開発形態: 職業訓練 卒業制作
- 関連プロジェクト: 旅香 - TABIKA -
- GitHub: <https://github.com/tonchiw3-tech/kousaku>

> 本プロジェクトは学習・卒業制作を目的としたアプリケーションです。実際の店舗予約サービスとしての運用を保証するものではありません。
