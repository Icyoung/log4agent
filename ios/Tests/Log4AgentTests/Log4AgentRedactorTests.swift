import XCTest
@testable import Log4Agent

final class Log4AgentRedactorTests: XCTestCase {
    func testRedactsSensitiveQueryParams() {
        let result = Log4AgentRedactor.redactURL(
            "https://example.com/path?token=abc&symbol=BTC-USDT&code=123"
        )

        XCTAssertEqual(
            result,
            "https://example.com/path?token=[REDACTED]&symbol=BTC-USDT&code=[REDACTED]"
        )
    }
}

