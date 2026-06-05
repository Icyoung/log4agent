import 'package:log4agent/log4agent.dart';
import 'package:test/test.dart';

void main() {
  test('redacts sensitive attributes', () {
    final result = Log4AgentRedactor.redactAttributes({
      'Authorization': 'Bearer abc',
      'symbol': 'BTC-USDT',
    });

    expect(result['Authorization'], Log4AgentRedactor.redacted);
    expect(result['symbol'], 'BTC-USDT');
  });

  test('redacts sensitive query params', () {
    final result = Log4AgentRedactor.redactUrl(
      'https://example.com/path?token=abc&symbol=BTC-USDT&code=123',
    );

    expect(
      result,
      'https://example.com/path?token=[REDACTED]&symbol=BTC-USDT&code=[REDACTED]',
    );
  });
}

