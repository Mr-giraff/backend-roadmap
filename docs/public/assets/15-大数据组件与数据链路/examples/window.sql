SET 'execution.runtime-mode' = 'streaming';

CREATE TEMPORARY TABLE lesson_clicks (
    user_id INT,
    event_time TIMESTAMP(3),
    WATERMARK FOR event_time AS event_time - INTERVAL '5' SECOND
) WITH (
    'connector' = 'filesystem',
    'path' = 'file:///tmp/study15-clicks.csv',
    'format' = 'csv',
    'csv.timestamp-format.standard' = 'SQL'
);

SELECT window_start, window_end, COUNT(*) AS click_count
FROM TABLE(
    TUMBLE(TABLE lesson_clicks, DESCRIPTOR(event_time), INTERVAL '1' MINUTE)
)
GROUP BY window_start, window_end;
