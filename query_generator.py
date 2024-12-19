from datetime import datetime, timedelta
import random

# 날짜 범위 설정
start_date = datetime(2024, 11, 1)
end_date = datetime(2025, 1, 31)

# 항공사 목록
countries = [['ICN', 'JFK', 13, 3, 700, 2000], ['JFK','ICN', 14, 3, 700, 2000], ['ICN','SYD', 10, 8, 1000, 1800], ['SYD','ICN', 10, 8, 1000, 1800], ['SYD','JFK', 24, 12, 800, 2800], ['JFK','SYD', 24, 12, 800, 2800], ['ICN','DOH', 4, 20, 300, 1200], ['DOH','ICN', 4, 20, 500, 1800], ['SYD','DOH', 14, 30, 900, 2200], ['DOH','SYD', 14, 30, 900, 2200], ['JFK','DOH', 12, 8, 900, 1200], ['DOH','JFK', 12, 8, 900, 1200]]
airlines = ['대한항공', '아시아나항공', '티웨이항공', '제주항공', '진에어항공','에어부산', '카타르항공']

# INSERT 쿼리 시작
query = "INSERT INTO Flight (departure_location, arrival_location, departure_date, arrival_date, airline, price) VALUES\n"
rows = []

for country in countries:
    departureLocation = country[0]
    arrivalLocation = country[1]
    defaultHour = country[2]
    errorHour = country[3]
    minPrice = country[4]
    maxPrice = country[5]
    current_date = start_date

    while current_date <= end_date:
        # 랜덤으로 4개의 항공사를 선택
        airline_cnt = random.randint(4,len(airlines))
        daily_airlines = random.sample(airlines, airline_cnt)
        
        for airline in daily_airlines:
            # 출발 및 도착 시간 생성
            randHour = random.randint(0, 23)
            departureDate = (current_date + timedelta(hours=randHour, minutes=random.randint(0, 11)*5)).strftime("%Y-%m-%d %H:%M")
            arrivalDate = (current_date + timedelta(hours=randHour+random.randint(defaultHour, defaultHour+errorHour), minutes=random.randint(0, 11)*5)).strftime("%Y-%m-%d %H:%M")
            price = random.randint(minPrice, maxPrice) * 1000
            
            # 데이터 추가
            rows.append(
                f"('{departureLocation}', '{arrivalLocation}', '{departureDate}', '{arrivalDate}', '{airline}', {price})"
            )
            
        
        # 하루 증가
        current_date += timedelta(days=1)

    # 쿼리 조합
query += ",\n".join(rows) + ";"

    # 출력
print(query)

