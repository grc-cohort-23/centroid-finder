const filename = 'Field clip #1%2F.mp4'
const nextVideo = 'Next clip.mov'
const filenameRoute = `/preview/${encodeURIComponent(filename)}`
const frame = '<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240"><rect width="320" height="240" fill="#999"/></svg>'

function preview(id, x = 79, y = 59) {
  return {
    originalUrl: `/previews/${id}/original.png`,
    previewUrl: `/previews/${id}/binarized.png`,
    width: 320,
    height: 240,
    centroid: { x, y },
  }
}

function selectVideo(video = filename) {
  cy.contains('li', video).find('button').click()
}

function setColor(value) {
  cy.get('input[type=color]').then(($input) => {
    const input = $input[0]
    const win = input.ownerDocument.defaultView
    Object.getOwnPropertyDescriptor(win.HTMLInputElement.prototype, 'value').set.call(input, value)
    input.dispatchEvent(new win.Event('input', { bubbles: true }))
  })
}

function checkMarkerPlacement() {
  cy.get('.centroid-marker').should('have.length', 2).each(($marker) => {
    const marker = $marker[0].getBoundingClientRect()
    const image = $marker.siblings('img')[0].getBoundingClientRect()
    expect(marker.left + marker.width / 2 - image.left).to.be.closeTo(image.width * 79.5 / 320, 1)
    expect(marker.top + marker.height / 2 - image.top).to.be.closeTo(image.height * 59.5 / 240, 1)
  })
}

beforeEach(() => {
  cy.intercept('GET', '/api/videos', [filename, nextVideo])
  cy.intercept('GET', '/previews/**', { headers: { 'content-type': 'image/svg+xml' }, body: frame })
})

it('navigates between videos, loads both frames and keeps both markers aligned on resize', () => {
  cy.intercept('POST', '/api/preview', (req) => {
    req.reply(preview(req.body.videoFilename === filename ? 'first' : 'second'))
  }).as('preview')

  cy.visit('/')
  cy.location('pathname').should('eq', '/videos')
  cy.contains('li', filename).find('a').should('have.attr', 'href', `/videos/${encodeURIComponent(filename)}`)
  selectVideo()
  cy.location('pathname').should('eq', filenameRoute)
  cy.wait('@preview').its('request.body').should('deep.equal', {
    videoFilename: filename,
    hexColor: '#FFA200',
    tolerance: 30,
  })
  cy.get('img[alt="Original frame"]').should('have.attr', 'src', '/previews/first/original.png')
  cy.get('img[alt="Binarized frame"]').should('have.attr', 'src', '/previews/first/binarized.png')
  cy.contains('Centroid (79, 59)').should('be.visible')
  checkMarkerPlacement()

  cy.viewport(440, 900)
  checkMarkerPlacement()
  cy.contains('label', 'Show centroid marker').click()
  cy.get('.centroid-marker').should('not.exist')
  cy.contains('label', 'Show centroid marker').click()
  cy.get('.centroid-marker').should('have.length', 2)

  cy.contains('a', 'All videos').click()
  selectVideo(nextVideo)
  cy.wait('@preview').its('request.body.videoFilename').should('eq', nextVideo)
  cy.get('img[alt="Original frame"]').should('have.attr', 'src', '/previews/second/original.png')
  cy.reload()
  cy.get('img[alt="Original frame"]').should('be.visible')
})

it('debounces threshold edits, sends color changes and omits the marker when Java reports no match', () => {
  cy.clock()
  cy.intercept('POST', '/api/preview', (req) => {
    const { tolerance } = req.body
    req.reply(tolerance === 0 ? preview('empty', -1, -1) : preview(String(tolerance)))
  }).as('preview')

  cy.visit(filenameRoute)
  cy.get('input[type=range]').should('exist')
  cy.tick(300)
  cy.wait('@preview')
  setColor('#0088ff')
  cy.get('input[type=range]').focus()
  cy.press(Cypress.Keyboard.Keys.LEFT)
  cy.press(Cypress.Keyboard.Keys.HOME)
  cy.get('input[type=range]').should('have.value', '0')
  cy.tick(299)
  cy.get('@preview.all').should('have.length', 1)
  cy.tick(1)
  cy.wait('@preview').its('request.body').should('deep.equal', {
    videoFilename: filename,
    hexColor: '#0088ff',
    tolerance: 0,
  })
  cy.get('img[alt="Binarized frame"]').should('have.attr', 'src', '/previews/empty/binarized.png')
  cy.contains('No centroid found with these settings.').should('be.visible')
  cy.get('.centroid-marker').should('not.exist')
})

it('aborts an in-flight preview when newer settings are selected', () => {
  let finishOldRequest
  const oldRequestGate = new Cypress.Promise((resolve) => { finishOldRequest = resolve })
  const oldRequestStarted = Cypress.sinon.spy()
  cy.intercept('POST', '/api/preview', (req) => {
    if (req.body.hexColor === '#FFA200') {
      oldRequestStarted()
      return oldRequestGate.then(() => req.reply(preview('old')))
    }
    req.reply(preview('new'))
  }).as('preview')

  cy.visit(filenameRoute, {
    onBeforeLoad(win) {
      const fetch = win.fetch
      cy.stub(win, 'fetch').callsFake((...args) => fetch.apply(win, args)).as('fetch')
    },
  })
  cy.wrap(oldRequestStarted).should('have.been.calledOnce')
  setColor('#0088ff')
  cy.get('img[alt="Binarized frame"]').should('have.attr', 'src', '/previews/new/binarized.png')
  cy.get('@fetch').should((fetch) => {
    const oldCall = fetch.getCalls().find(({ args }) => args[0] === '/api/preview')
    expect(oldCall.args[1].signal.aborted).to.equal(true)
  })
  cy.then(() => finishOldRequest())
  cy.get('img[alt="Binarized frame"]').should('have.attr', 'src', '/previews/new/binarized.png')
})

it('keeps the live mask visible until the new image and centroid can be shown together', () => {
  let releaseImage
  const imageGate = new Cypress.Promise((resolve) => { releaseImage = resolve })
  const newImageRequested = Cypress.sinon.spy()
  const originalRequests = Cypress.sinon.spy()

  cy.intercept('GET', '/previews/*/original.png', (req) => {
    originalRequests()
    req.reply({ headers: { 'content-type': 'image/svg+xml' }, body: frame })
  })
  cy.intercept('GET', '/previews/updated/binarized.png', (req) => {
    newImageRequested()
    return imageGate.then(() => req.reply({ headers: { 'content-type': 'image/svg+xml' }, body: frame }))
  })
  cy.intercept('POST', '/api/preview', (req) => {
    req.reply(req.body.hexColor === '#FFA200' ? preview('initial') : preview('updated', 50, 90))
  })

  cy.visit(filenameRoute)
  checkMarkerPlacement()
  setColor('#999999')
  cy.wrap(newImageRequested).should('have.been.calledOnce')
  cy.get('img[alt="Binarized frame"]').should('have.attr', 'src', '/previews/initial/binarized.png')
  cy.get('canvas.live-mask').should('be.visible').and(($canvas) => {
    const pixel = $canvas[0].getContext('2d').getImageData(0, 0, 1, 1).data
    expect(Array.from(pixel)).to.deep.equal([255, 255, 255, 255])
  })
  cy.get('.centroid-marker').should('not.exist')
  cy.contains('Updating preview…').should('be.visible')

  cy.then(() => releaseImage())
  cy.get('img[alt="Binarized frame"]').should('have.attr', 'src', '/previews/updated/binarized.png')
  cy.get('canvas.live-mask').should('not.be.visible')
  cy.contains('Centroid (50, 90)').should('be.visible')
  cy.get('.centroid-marker').should('have.length', 2)
  cy.wrap(originalRequests).should('have.been.calledOnce')
})

it('submits a job, polls until it is complete and exposes the CSV download', () => {
  let polls = 0
  cy.intercept('POST', '/api/preview', preview('ready'))
  cy.intercept('POST', '/jobs', { statusCode: 202, body: { jobId: 'test-job', status: 'processing' } }).as('start')
  cy.intercept('GET', '/jobs/test-job', (req) => {
    polls++
    req.reply(polls === 1 ? { status: 'processing' } : { status: 'completed', downloadUrl: '/outputs/test.csv' })
  }).as('poll')
  cy.intercept('GET', '/outputs/test.csv', {
    headers: { 'content-type': 'text/csv' },
    body: '0.0,79,59\n',
  }).as('csv')

  cy.visit(filenameRoute)
  cy.contains('button', 'Process Video with These Settings').click()
  cy.wait('@start').its('request.body').should('deep.equal', {
    videoFilename: filename,
    hexColor: '#FFA200',
    tolerance: 30,
  })
  cy.contains('Processing video…').should('be.visible')
  cy.wait('@poll')
  cy.wait('@poll')
  cy.contains('Processing complete.').should('be.visible')
  cy.contains('a', 'Download CSV').should(($link) => {
    expect($link).to.have.attr('href', '/outputs/test.csv')
    expect($link).to.have.attr('download')
  }).click()
  cy.wait('@csv')
})

it('shows preview errors and job failures and lets the user try again', () => {
  cy.intercept('POST', '/api/preview', { statusCode: 500, body: { error: 'Could not decode the video.' } })
  cy.intercept('POST', '/jobs', { statusCode: 202, body: { jobId: 'failed-job', status: 'processing' } })
  cy.intercept('GET', '/jobs/failed-job', { status: 'failed', error: 'Video processing failed.' })

  cy.visit(filenameRoute)
  cy.contains('[role=alert]', 'Could not decode the video.').should('be.visible')
  cy.contains('button', 'Process Video with These Settings').click()
  cy.contains('[role=alert]', 'Video processing failed.').should('be.visible')
  cy.contains('button', 'Process Video with These Settings').should('be.enabled')
})
