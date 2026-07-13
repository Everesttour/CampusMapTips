import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
  Building2,
  ChevronRight,
  Grip,
  Info,
  Lightbulb,
  MapPin,
  Minus,
  MousePointer2,
  Plus,
  Search,
  Sparkles,
  X,
} from 'lucide-react'
import './App.css'

type PlaceKind = 'building' | 'facility'

type Building = {
  id: number
  xLocation: number
  yLocation: number
  name: string
  description: string
  rooms: string
}

type Facility = {
  id: number
  xLocation: number
  yLocation: number
  floor: number
  name: string
  buildingName: string
  overview: string
  description: string
  notice: string
  operatingHours: string
  tips: Array<string | null>
}

type Place = Building | Facility
type PlaceModal = { type: 'place'; placeKind: PlaceKind; place: Place }
type Modal = PlaceModal | { type: 'app-info' } | null
type Point = { x: number; y: number }

const MAP_WIDTH = 1600
const MAP_HEIGHT = 1125
const MIN_ZOOM = 1
const MAX_ZOOM = 2.5
const FACILITY_ZOOM_THRESHOLD = 1.05
const repositoryUrl = 'https://github.com/Everesttour/CampusMapTips'

const meta = {
  building: { label: '건물', icon: Building2 },
  facility: { label: '시설', icon: Sparkles },
} as const

function assetPath(path: string) {
  return `${import.meta.env.BASE_URL}${path}`
}

async function loadJson<T>(path: string): Promise<T> {
  const response = await fetch(assetPath(path))
  if (!response.ok) throw new Error('안내 데이터를 불러오지 못했습니다.')
  return response.json() as Promise<T>
}

function isFacility(place: Place): place is Facility {
  return 'overview' in place
}

function floorLabel(floor: number) {
  if (floor === 0) return '정보 없음'
  return floor < 0 ? `지하 ${Math.abs(floor)}층` : `${floor}층`
}

function containsQuery(place: Place, query: string) {
  const normalized = query.trim().toLocaleLowerCase('ko-KR')
  if (!normalized) return true

  const fields = isFacility(place)
    ? [place.name, place.buildingName, place.overview, place.description, place.notice]
    : [place.name, place.description, place.rooms]

  return fields.join(' ').toLocaleLowerCase('ko-KR').includes(normalized)
}

function App() {
  const [buildings, setBuildings] = useState<Building[]>([])
  const [facilities, setFacilities] = useState<Facility[]>([])
  const [activeKind, setActiveKind] = useState<PlaceKind>('building')
  const [query, setQuery] = useState('')
  const [modal, setModal] = useState<Modal>(null)
  const [zoom, setZoom] = useState(MIN_ZOOM)
  const [pan, setPan] = useState<Point>({ x: 0, y: 0 })
  const [isDragging, setIsDragging] = useState(false)
  const [loadError, setLoadError] = useState('')
  const viewportRef = useRef<HTMLDivElement>(null)
  const dragRef = useRef<{ pointerId: number; clientX: number; clientY: number; pan: Point } | null>(null)

  useEffect(() => {
    Promise.all([loadJson<Building[]>('data/buildings.json'), loadJson<Facility[]>('data/facilities.json')])
      .then(([buildingData, facilityData]) => {
        setBuildings(buildingData.sort((a, b) => a.id - b.id))
        setFacilities(facilityData.sort((a, b) => a.id - b.id))
      })
      .catch((error: unknown) => setLoadError(error instanceof Error ? error.message : '안내 데이터를 불러오지 못했습니다.'))
  }, [])

  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setModal(null)
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [])

  const clampPan = useCallback((nextPan: Point, nextZoom: number) => {
    const bounds = viewportRef.current?.getBoundingClientRect()
    if (!bounds) return nextPan
    const maxX = (bounds.width * (nextZoom - 1)) / 2
    const maxY = (bounds.height * (nextZoom - 1)) / 2
    return {
      x: Math.min(maxX, Math.max(-maxX, nextPan.x)),
      y: Math.min(maxY, Math.max(-maxY, nextPan.y)),
    }
  }, [])

  const zoomAt = useCallback((nextZoom: number, focus: Point = { x: 0, y: 0 }) => {
    const boundedZoom = Math.min(MAX_ZOOM, Math.max(MIN_ZOOM, nextZoom))
    setPan((currentPan) => {
      const nextPan = {
        x: focus.x - ((focus.x - currentPan.x) / zoom) * boundedZoom,
        y: focus.y - ((focus.y - currentPan.y) / zoom) * boundedZoom,
      }
      return clampPan(nextPan, boundedZoom)
    })
    setZoom(boundedZoom)
  }, [clampPan, zoom])

  const focusOn = useCallback((place: Place) => {
    const bounds = viewportRef.current?.getBoundingClientRect()
    const targetZoom = MAX_ZOOM
    if (bounds) {
      const coordinate = {
        x: ((place.xLocation / MAP_WIDTH) - 0.5) * bounds.width,
        y: ((place.yLocation / MAP_HEIGHT) - 0.5) * bounds.height,
      }
      setPan(clampPan({ x: -coordinate.x * targetZoom, y: -coordinate.y * targetZoom }, targetZoom))
    }
    setZoom(targetZoom)
  }, [clampPan])

  const openPlace = useCallback((placeKind: PlaceKind, place: Place, shouldFocus = true) => {
    if (shouldFocus) focusOn(place)
    setModal({ type: 'place', placeKind, place })
  }, [focusOn])

  const selectKind = (kind: PlaceKind) => {
    setActiveKind(kind)
    setModal(null)
    if (kind === 'building') {
      setPan({ x: 0, y: 0 })
      setZoom(MIN_ZOOM)
    } else {
      zoomAt(FACILITY_ZOOM_THRESHOLD)
    }
  }

  const onMapWheel = (event: React.WheelEvent<HTMLDivElement>) => {
    event.preventDefault()
    const bounds = event.currentTarget.getBoundingClientRect()
    const focus = { x: event.clientX - bounds.left - (bounds.width / 2), y: event.clientY - bounds.top - (bounds.height / 2) }
    const multiplier = event.deltaY < 0 ? 1.18 : 1 / 1.18
    zoomAt(zoom * multiplier, focus)
  }

  const onPointerDown = (event: React.PointerEvent<HTMLDivElement>) => {
    if (event.button !== 0 || (event.target as HTMLElement).closest('button')) return
    dragRef.current = { pointerId: event.pointerId, clientX: event.clientX, clientY: event.clientY, pan }
    event.currentTarget.setPointerCapture(event.pointerId)
    setIsDragging(true)
  }

  const onPointerMove = (event: React.PointerEvent<HTMLDivElement>) => {
    const drag = dragRef.current
    if (!drag || drag.pointerId !== event.pointerId) return
    setPan(clampPan({ x: drag.pan.x + event.clientX - drag.clientX, y: drag.pan.y + event.clientY - drag.clientY }, zoom))
  }

  const endPointerDrag = (event: React.PointerEvent<HTMLDivElement>) => {
    if (dragRef.current?.pointerId === event.pointerId) {
      dragRef.current = null
      setIsDragging(false)
    }
  }

  const displayedPlaces = useMemo(() => {
    const list = activeKind === 'building' ? buildings : facilities
    return list.filter((place) => containsQuery(place, query))
  }, [activeKind, buildings, facilities, query])

  const activePlace = modal?.type === 'place' ? modal : null
  const showFacilities = zoom >= FACILITY_ZOOM_THRESHOLD
  const isLoading = !loadError && buildings.length === 0 && facilities.length === 0

  if (isLoading) return <main className="loading-screen">캠퍼스 지도를 준비하고 있습니다.</main>

  if (loadError) {
    return <main className="loading-screen error-state"><Info size={22} /><p>{loadError}</p><a href={repositoryUrl}>저장소에서 확인하기</a></main>
  }

  return (
    <main className="app-shell">
      <aside className="navigation-panel" aria-label="장소 목록">
        <div className="brand-block">
          <span className="brand-mark" aria-hidden="true"><MapPin size={21} fill="currentColor" /></span>
          <div><p className="eyebrow">SUCCESS CAMPUS</p><h1>캠퍼스 꿀팁 지도</h1></div>
        </div>

        <div className="kind-tabs" role="tablist" aria-label="장소 종류">
          {(Object.keys(meta) as PlaceKind[]).map((kind) => {
            const Icon = meta[kind].icon
            const count = kind === 'building' ? buildings.length : facilities.length
            return (
              <button key={kind} className={`kind-tab ${activeKind === kind ? 'is-active' : ''}`} type="button" role="tab" aria-selected={activeKind === kind} onClick={() => selectKind(kind)}>
                <Icon size={18} aria-hidden="true" /><span>{meta[kind].label}</span><em>{count}</em>
              </button>
            )
          })}
        </div>

        <label className="search-field">
          <Search size={17} aria-hidden="true" />
          <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="장소 이름으로 검색" aria-label="장소 검색" />
          {query && <button type="button" className="clear-search" onClick={() => setQuery('')} aria-label="검색어 지우기"><X size={16} /></button>}
        </label>

        <div className="list-heading"><p>{activeKind === 'building' ? '건물 목록' : '시설 목록'}</p><span>{displayedPlaces.length}</span></div>
        <div className="place-list" role="list">
          {displayedPlaces.map((place) => {
            const selected = activePlace?.placeKind === activeKind && activePlace.place.id === place.id
            const Icon = meta[activeKind].icon
            const subtitle = isFacility(place) ? `${place.buildingName || '교외 시설'} · ${floorLabel(place.floor)}` : place.description
            return (
              <button className={`place-row ${selected ? 'is-selected' : ''}`} type="button" role="listitem" key={place.id} onClick={() => openPlace(activeKind, place)}>
                <span className={`place-icon ${activeKind}`}><Icon size={17} /></span>
                <span className="place-copy"><strong>{place.name}</strong><small>{subtitle}</small></span>
                <ChevronRight size={17} aria-hidden="true" />
              </button>
            )
          })}
          {!displayedPlaces.length && <div className="empty-results"><Search size={20} /><p>찾는 장소가 없어요.</p><button type="button" onClick={() => setQuery('')}>검색어 지우기</button></div>}
        </div>
      </aside>

      <section className="map-panel" aria-label="성공회대학교 캠퍼스 지도">
        <header className="map-header">
          <div><p className="eyebrow">CAMPUS MAP</p><h2>지도를 드래그하고 휠로 확대해 보세요.</h2></div>
          <div className="map-actions">
            <span className={`facility-status ${showFacilities ? 'is-on' : ''}`}><Sparkles size={15} />{showFacilities ? '시설 핀 표시 중' : '1.05배부터 시설 핀 표시'}</span>
            <div className="zoom-control" aria-label="지도 확대 축소">
              <button type="button" disabled={zoom <= MIN_ZOOM} onClick={() => zoomAt(zoom / 1.18)} aria-label="지도 축소"><Minus size={16} /></button>
              <output>{Math.round(zoom * 100)}%</output>
              <button type="button" disabled={zoom >= MAX_ZOOM} onClick={() => zoomAt(zoom * 1.18)} aria-label="지도 확대"><Plus size={16} /></button>
            </div>
          </div>
        </header>

        <div className="map-stage">
          <button className="map-info-button" type="button" onClick={() => setModal({ type: 'app-info' })} aria-label="성공회대학교 시설 가이드 앱 정보"><Info size={19} /></button>
          <div
            className={`map-viewport ${isDragging ? 'is-dragging' : ''}`}
            ref={viewportRef}
            onWheel={onMapWheel}
            onPointerDown={onPointerDown}
            onPointerMove={onPointerMove}
            onPointerUp={endPointerDrag}
            onPointerCancel={endPointerDrag}
          >
            <div className="map-canvas" style={{ transform: `translate(${pan.x}px, ${pan.y}px) scale(${zoom})` }}>
              <img className="campus-map" src={assetPath('images/map.webp')} alt="성공회대학교 캠퍼스 조감 지도" draggable="false" />
              {buildings.map((building) => {
                const selected = activePlace?.placeKind === 'building' && activePlace.place.id === building.id
                return <button key={building.id} className={`building-target ${selected ? 'is-selected' : ''}`} type="button" style={{ left: `${(building.xLocation / MAP_WIDTH) * 100}%`, top: `${(building.yLocation / MAP_HEIGHT) * 100}%` }} onClick={() => openPlace('building', building, false)} aria-label={`${building.name} 상세 보기`} />
              })}
              {showFacilities && facilities.map((facility) => {
                const selected = activePlace?.placeKind === 'facility' && activePlace.place.id === facility.id
                return <button key={facility.id} className={`facility-pin ${selected ? 'is-selected' : ''}`} type="button" style={{ left: `${(facility.xLocation / MAP_WIDTH) * 100}%`, top: `${(facility.yLocation / MAP_HEIGHT) * 100}%` }} onClick={() => openPlace('facility', facility, false)} aria-label={`${facility.name} 상세 보기`}><span>{facility.name}</span></button>
              })}
            </div>
          </div>
          <div className="map-legend"><span><i className="building-dot" /> 건물 번호</span><span><i className="facility-dot" /> 시설 핀</span></div>
          <p className="map-guide"><Grip size={15} aria-hidden="true" /> 드래그로 이동 <span>·</span> <MousePointer2 size={15} aria-hidden="true" /> 휠로 확대</p>
        </div>
        <footer className="map-footer"><Info size={15} aria-hidden="true" /> 정보는 변경될 수 있습니다. 방문 전 운영시간을 한 번 더 확인해 주세요.</footer>
      </section>

      {modal?.type === 'place' && <PlaceDetailModal modal={modal} onClose={() => setModal(null)} />}
      {modal?.type === 'app-info' && <AppInfoModal onClose={() => setModal(null)} />}
    </main>
  )
}

function PlaceDetailModal({ modal, onClose }: { modal: PlaceModal; onClose: () => void }) {
  const { place, placeKind } = modal
  const facility = placeKind === 'facility' ? place as Facility : null
  const building = placeKind === 'building' ? place as Building : null
  const imagePath = assetPath(`images/${placeKind === 'building' ? 'buildings' : 'facilities'}/${place.id}.webp`)

  return (
    <div className="modal-backdrop" role="presentation">
      <div className="detail-modal" role="dialog" aria-modal="true" aria-label={`${place.name} 상세 정보`}>
        <figure className="detail-image"><img src={imagePath} alt="" onError={(event) => event.currentTarget.parentElement?.classList.add('image-unavailable')} /><figcaption>이미지 없음</figcaption></figure>
        {building ? <BuildingDetail building={building} onClose={onClose} /> : <FacilityDetail facility={facility!} onClose={onClose} />}
      </div>
    </div>
  )
}

function BuildingDetail({ building, onClose }: { building: Building; onClose: () => void }) {
  const rooms = building.rooms.split(' / ').map((room) => room.trim()).filter(Boolean)
  return (
    <section className="legacy-panel building-panel">
      <button className="modal-close" type="button" onClick={onClose} aria-label="상세 정보 닫기"><X size={19} /></button>
      <p className="detail-id">건물번호: {building.id}</p>
      <h3>{building.name}</h3>
      <div className="legacy-scroll building-scroll">
        <h4><MapPin size={19} aria-hidden="true" /> 주요 시설</h4>
        {rooms.length ? rooms.map((room, index) => {
          const divider = room.indexOf(':')
          const title = divider > 0 ? room.slice(0, divider) : '주요 공간'
          const content = divider > 0 ? room.slice(divider + 1).trim() : room
          return <article className="room-block" key={`${title}-${index}`}><strong>{title}</strong><p>{content}</p></article>
        }) : <article className="room-block"><p>등록된 주요 시설 정보가 없습니다.</p></article>}
      </div>
    </section>
  )
}

function FacilityDetail({ facility, onClose }: { facility: Facility; onClose: () => void }) {
  const tips = facility.tips.filter((tip): tip is string => Boolean(tip?.trim()))
  return (
    <section className="legacy-panel facility-panel">
      <button className="modal-close" type="button" onClick={onClose} aria-label="상세 정보 닫기"><X size={19} /></button>
      <div className="facility-layout">
        <section className="facility-info-card">
          <span className="card-title">시설 정보</span>
          <h3>{facility.name}</h3>
          <p className="facility-building">건물: {facility.buildingName || '-'}</p>
          <div className="legacy-scroll facility-scroll">
            <InfoBlock title="개요" content={facility.overview || '개요 정보가 없습니다.'} large />
            <InfoBlock title="공지사항" content={facility.notice || '정보 없음'} />
            <InfoBlock title="층수" content={floorLabel(facility.floor)} />
            <InfoBlock title="운영 시간" content={facility.operatingHours || '정보 없음'} />
            <InfoBlock title="설명" content={facility.description || '설명 정보가 없습니다.'} large />
          </div>
        </section>
        <section className="tips-card">
          <h4><Lightbulb size={18} aria-hidden="true" /> 꿀팁</h4>
          <div className="legacy-scroll tips-scroll">
            {tips.length ? <ul>{tips.map((tip, index) => <li key={`${tip}-${index}`}>{tip}</li>)}</ul> : <p>등록된 꿀팁이 없습니다.</p>}
          </div>
        </section>
      </div>
    </section>
  )
}

function InfoBlock({ title, content, large = false }: { title: string; content: string; large?: boolean }) {
  return <article className={`info-block ${large ? 'is-large' : ''}`}><strong>{title}</strong><p>{content}</p></article>
}

function AppInfoModal({ onClose }: { onClose: () => void }) {
  return (
    <div className="modal-backdrop info-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose() }}>
      <section className="app-info-modal" role="dialog" aria-modal="true" aria-label="성공회대학교 시설 가이드 앱 정보">
        <button className="modal-close" type="button" onClick={onClose} aria-label="앱 정보 닫기"><X size={19} /></button>
        <h3>🗺️ 성공회대학교 시설 가이드 앱 정보</h3>
        <div className="app-info-scroll">
          <InfoSection title="1. 앱 소개 (Welcome)">
            환영합니다! 이 앱은 성공회대학교 캠퍼스 내 주요 시설의 위치, 상세 정보, 그리고 유용한 ‘꿀팁’을 빠르고 쉽게 찾아볼 수 있도록 제작되었습니다. 학교 생활을 편리하게 만드는 데 도움이 되기를 바랍니다.
          </InfoSection>
          <InfoSection title="2. 주요 기능 및 사용 방법">
            <ol>
              <li><strong>시설 선택:</strong> 지도 상의 번호 아이콘을 클릭하거나 좌측 목록에서 원하는 장소를 선택합니다.</li>
              <li><strong>상세 정보 확인:</strong> 선택한 장소의 개요, 공지사항, 층수, 운영 시간, 설명을 확인합니다.</li>
              <li><strong>꿀팁 활용:</strong> 시설 상세 창 오른쪽의 꿀팁 영역에서 학생들의 이용 팁을 확인합니다.</li>
              <li><strong>지도 조작:</strong> 지도를 드래그해 이동하고, 마우스 휠로 확대·축소합니다. 1.05배 이상 확대하면 시설 핀이 나타납니다.</li>
            </ol>
          </InfoSection>
          <InfoSection title="3. 아이콘 범례 (Legend)">
            <p><strong>01~13</strong> 주요 건물 · 승연관, 일만관, 월당관, 나눔관, 이천관기념관, 새천년관, 중앙도서관 등</p>
            <p><strong>14</strong> 외부 시설</p>
          </InfoSection>
          <InfoSection title="4. 앱 개발 정보">
            <p>버전: 1.0.0 (2025년 11월)</p>
            <p>제작: 김주환 · 김상윤 · 김준 · 조민성</p>
            <p>문의: kimjuhwan6315@naver.com</p>
          </InfoSection>
        </div>
      </section>
    </div>
  )
}

function InfoSection({ title, children }: { title: string; children: React.ReactNode }) {
  return <section className="app-info-section"><h4>{title}</h4><div>{children}</div></section>
}

export default App
